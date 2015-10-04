#!/usr/bin/python -S
# Automatically generated on Sun Mar  1 16:17:32 2015

'''Runs a specific user program'''


import sys
sys.path[0:0] = [
  '/home/dingz/bob.spear-1.1.8/eggs/gridtk-1.2.0-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/six-1.9.0-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8',
  '/home/dingz/bob.spear-1.1.8/eggs/xbob.sox-1.1.0-py2.7-linux-x86_64.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/xbob.db.verification.filelist-1.3.5-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/facereclib-1.2.3-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/xbob.db.verification.utils-1.0.1-py2.7.egg',
  '/home/dingz/bob.spear-1.1.8/eggs/xbob.db.atnt-1.1.2-py2.7.egg',
  ]
import site #initializes site properly
site.main() #this is required for python>=3.4
import pkg_resources #initializes virtualenvs properly

import facereclib.script.evaluate

if __name__ == '__main__':
    sys.exit(facereclib.script.evaluate.main())
