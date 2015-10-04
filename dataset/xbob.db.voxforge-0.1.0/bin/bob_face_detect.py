#!/usr/bin/python -S
# Automatically generated on Sun Mar 22 17:13:51 2015

'''Runs a specific user program'''


import sys
sys.path[0:0] = [
  '/home/dingz/dataset/xbob.db.voxforge-0.1.0/eggs/xbob.db.verification.filelist-1.3.5-py2.7.egg',
  '/home/dingz/dataset/xbob.db.voxforge-0.1.0/eggs/xbob.db.verification.utils-1.0.1-py2.7.egg',
  '/home/dingz/dataset/xbob.db.voxforge-0.1.0/eggs/six-1.9.0-py2.7.egg',
  '/home/dingz/dataset/xbob.db.voxforge-0.1.0',
  '/home/dingz/dataset/xbob.db.voxforge-0.1.0/eggs/vanity-2.0.5-py2.7.egg',
  ]
import site #initializes site properly
site.main() #this is required for python>=3.4
import pkg_resources #initializes virtualenvs properly

import bob.visioner.script.facebox

if __name__ == '__main__':
    sys.exit(bob.visioner.script.facebox.main())
