from __future__ import print_function

##DEPS io.leego:banana:2.1.0
##JYTHON 2.7.3

import io.leego.banana.BananaUtils as BananaUtils
import io.leego.banana.Font as Font

text0 = "Jython 2.7"
text1 = BananaUtils.bananaify(text0, Font.STANDARD)

print(text1)
