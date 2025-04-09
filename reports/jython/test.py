from __future__ import print_function
import sys

# https://github.com/jbangdev/jbang/issues/1911
# https://github.com/jython/jython/issues/371

# https://stackoverflow.com/questions/16701979/packaging-a-jython-program-in-an-executable-jar

##DEPS io.leego:banana:2.1.0
##JYTHON 2.7.4
##JAVA 21

import io.leego.banana.BananaUtils as BananaUtils
import io.leego.banana.Font as Font


def main():
    print(sys.argv)

    text0 = "Jython 2.7"
    text1 = BananaUtils.bananaify(text0, Font.STANDARD)

    print(text1)

main()