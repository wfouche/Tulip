import requests

# Get a single post
response = requests.get('https://jsonplaceholder.typicode.com/posts/1')
post_data = response.json()
print("Single Post:")
print(post_data)

# Get all posts
response_all = requests.get('https://jsonplaceholder.typicode.com/posts')
all_posts_data = response_all.json()
print("\nAll Posts (first 3):")
print(all_posts_data[:3])

# Making POST Requests (Creating Resources)

new_post_payload = {
    'title': 'foo',
    'body': 'bar',
    'userId': 1,
}

response_post = requests.post('https://jsonplaceholder.typicode.com/posts', json=new_post_payload)
created_post = response_post.json()
print("\nCreated Post:")
print(created_post)

# PUT and PATCH

# Update a post (PUT - full replacement)
updated_post_payload = {
    'id': 1,
    'title': 'new title',
    'body': 'new body',
    'userId': 1,
}
response_put = requests.put('https://jsonplaceholder.typicode.com/posts/1', json=updated_post_payload)
updated_post_put = response_put.json()
print("\nUpdated Post (PUT):")
print(updated_post_put)

# Patch a post (PATCH - partial update)
patch_payload = {
    'title': 'patched title',
}
response_patch = requests.patch('https://jsonplaceholder.typicode.com/posts/1', json=patch_payload)
patched_post = response_patch.json()
print("\nPatched Post:")
print(patched_post)

# DELETE

response_delete = requests.delete('https://jsonplaceholder.typicode.com/posts/1')
print("\nDelete Status Code:")
print(response_delete.status_code) # Should be 200 OK