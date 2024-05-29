require 'asciidoctor/extensions' unless RUBY_ENGINE == 'opal'

include Asciidoctor

# Treeprocessor that adds external callout marks to source code
#
# [source, ruby]
# ----
# puts 'Hello World'
# puts 'Hello again'
# ---
# 1. Saying hello @1
# 2. And again @2
# 3. Text search @/again/

Asciidoctor::Extensions::register do

  CALLOUT_SOURCE_BLOCK_ROLE ||= 'external-callout-block'
  CALLOUT_ORDERED_LIST_ROLE ||= 'external-callout-list'

  STANDALONE_CALLOUT_LIST_STYLE ||= 'calloutlist'

  LOCATION_TOKEN_RX ||= /@(\d+)|(@\/(?:\\\/|[^\/])+?\/[ig]{0,2})/
  LOCATION_TOKEN_ARRAY_RX ||= /^(@\d+|@\/(?:\\\/|[^\/]|)+?\/[ig]{0,2})((\s+@\d+)|(\s+@\/(?:\\\/|[^\/]|)+?\/[ig]{0,2}))*$/

  SEARCH_STRING_RX ||= /\/((?:\\\/|[^\/])+?)\//
  SEARCH_OPTIONS_RX ||= /\/(?:\\\/|[^\/])+?\/([ig]{0,2})/

  tree_processor do

    process do |document|

      begin
        # set the traverse_documents option to look for ordered lists
        # The makes sure it's the right kind of list.
        document.find_by context: :olist do |list|

          # if there is as calloutlist style attached to the block
          # then simply style the block as a colist. This will allow folk
          # to create callout blocks that can be attached to annotated images etc.

          if list.style.include? STANDALONE_CALLOUT_LIST_STYLE

            list.context = :colist

          else

            if external_callout_list? list

              owner_block = owning_block document, list

              owner_block.subs.replace(owner_block.subs + [:callouts]).uniq

              process_callouts(list, owner_block)

              list.context = :colist

              owner_block.add_role(CALLOUT_SOURCE_BLOCK_ROLE) unless owner_block.has_role?(CALLOUT_SOURCE_BLOCK_ROLE)
              list.add_role(CALLOUT_ORDERED_LIST_ROLE) unless list.has_role?(CALLOUT_ORDERED_LIST_ROLE)

            end

          end

        end

      rescue => e
        warn e.message
      ensure

        document

      end

    end

    # Checks the format of the list
    # to make sure it's a callout list we should be
    # dealing with.
    # We also won't want any nested lists.
    def external_callout_list?(list)

      list.blocks.all? do |x|

        item_under_test = x.instance_variable_get(:@text)
        location_token_index = item_under_test.index(LOCATION_TOKEN_RX)

        # if we don't find the start of the list of location tokens, or the token is the first item
        # then we don't need to carry on any further; this is not the list we are looking for.
        return false if location_token_index == nil or location_token_index == 0

        # just look at the string of location tokens.
        location_tokens = item_under_test[location_token_index..-1].strip
        location_tokens.match(LOCATION_TOKEN_ARRAY_RX) && x.blocks.empty?

      end

    end

    # Have a look at the next level up
    # to find the block that our CO list belongs to
    def owning_block(document, list)

      all_blocks = document.find_by

      raise "There are no available blocks" if all_blocks == nil

      index_back = all_blocks.index { |x| x == list }

      # We should be able to find our own block, but in case we can't …
      raise "Error – could not locate our ordered list" if index_back == nil

      while index_back > 0

        index_back = index_back - 1

        # We have found our matching block
        return all_blocks[index_back] if all_blocks[index_back].context == :listing

        # We have hit another callout list, but there was no list block first.
        # Assume we have an error
        raise "Callout list found while seeking listing block" if all_blocks[index_back].context == :colist

      end

      # If we didn't find a listing then this document has probably got
      # bits missing.
      raise "No listing found"

    end

    def find_list_index_for_item (list_item)

      list = list_item.parent

      list_numeral = 1
      index_of_this_item = list.blocks.index { |x| x == list_item }
      index_of_this_item.times { list_numeral = list_numeral.next }
      list_numeral

    end

    def process_callouts(list, owner_block)
      list.blocks.each do |list_item|

        # Don't call `list_item.text` because this returns
        # the string in a formatted form. We want the
        # raw string, so go straight to the instance variable.
        item = list_item.instance_variable_get(:@text)

        # No need to test the matcher; we tested
        # the whole block of items when we started

        location_token_index = item.index(LOCATION_TOKEN_RX)
        location_tokens = item[location_token_index..-1].strip
        phrase = item[0..location_token_index - 1].strip

        locations = location_tokens.scan(LOCATION_TOKEN_RX).flatten.compact

        line_numbers = Set.new

        locations.each do |location|

          if location.is_numeric?

            line_number = location.to_i

            if line_number.between?(1, owner_block.lines.length)
              line_numbers << (line_number - 1)  # Because the line number is now an array index, so -1
            else
              warn "Line number out of range ==> #{line_number}"
            end

          else

            # Must be a string matcher then
            search_options = get_search_options location

            search_string = location[SEARCH_STRING_RX, 1]
            found_line_numbers = find_matching_lines(search_string, search_options[:global_search],
                                                     search_options[:case_insensitive], owner_block)

            if !found_line_numbers.empty?

              line_numbers = line_numbers.merge(found_line_numbers)

            else
              warn "Search term not found ==> #{location}"
            end

          end

        end

        line_numbers.each do |line_number|

          callout = find_list_index_for_item(list_item)
          owner_block.lines[line_number] += " <#{callout}>"

        end

        list_item.text = phrase

      end

    end

    def find_matching_lines(search_string, global_search, case_insensitive, owner_block)

      if case_insensitive
        string_to_match = Regexp.new(search_string, Regexp::IGNORECASE)
      else
        string_to_match = Regexp.new(search_string)
      end
      found_lines = Set.new

      owner_block.lines.each_with_index do |line, index|

        if line.match(string_to_match) != nil

          found_lines << index

          return found_lines unless global_search

        end

      end

      found_lines

    end

    def get_search_options(location)

      options = { :case_insensitive => false,
                  :global_search => false }

      matches = location.match(SEARCH_OPTIONS_RX)

      return options unless matches

      # This is just for completeness, but make sure that
      # none of the options are mentioned twice in the list

      raise "Invalid search options: #{matches[1]}" if matches[1].match /^(.).*\1$/

      options[:case_insensitive] = true if matches[1].include? 'i'
      options[:global_search] = true if matches[1].include? 'g'

      return options

    end

    class String
      def is_numeric?
        self.match(/^\d+$/)
      end
    end

  end

end





