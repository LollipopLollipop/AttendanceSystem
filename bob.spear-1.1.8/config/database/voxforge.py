#!/usr/bin/env python

import xbob.db.voxforge

# 0/ The database to use
name = 'VoxForge'
# put the full path to the database filelist
db = xbob.db.voxforge.Database()
#db = None
protocol = None

# directory where the wave files are stored
wav_input_dir = "/home/dingz/dataset/audiofiles/"
wav_input_ext = ".wav"

