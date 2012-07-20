import os
import sys
import stat

from Tkinter import *
from tkMessageBox import *


CLEAN_TARGETS = ["obj", "sbr", "pdb", "idb", "tli", "tlh", "exp", "plg", "bsc",
                 "ncb", "sup", "ilk", "log", "klg", "aps", "pch", "res", "o"]


def cur_file_dir():
    path = sys.path[0]
    return path


def confirm(dir):
    choice = askquestion('Project Cleaner', 'Are you sure to clean the project?')
    if choice=='yes':
        print "Now clean "+dir
    else:
        print "Now quit..."
        sys.exit()

def removeall(dir_file):
    os.chmod(dir_file, stat.S_IREAD|stat.S_IWRITE);
    if os.path.isdir(dir_file):
        for root, dirs, files in os.walk(dir_file, topdown=False):
            for name in files:
                path = os.path.join(root, name)
                os.chmod(path, stat.S_IREAD|stat.S_IWRITE);
                os.remove(path)
            for name in dirs:
                path = os.path.join(root, name)
                os.chmod(path, stat.S_IREAD|stat.S_IWRITE);
                os.rmdir(path)

        os.rmdir(dir_file)
    else:
        os.unlink(dir_file)
    return

def clean(dir):
    if not os.path.isdir(dir):
        return

    # delete SVN record
    dirNames = dir.split(os.path.sep)
    lastName = dirNames[len(dirNames)-1]
    lastName = lastName.lower()
    if lastName=='.svn':
       removeall(dir)
       print "  Remove " + dir
       return

    paths = None
    try:
        paths = os.listdir(dir)

        for item in paths:
            filePath = os.path.join(dir, item)

            #check if the file should be deleted
            if os.path.isfile( filePath ):
                ext = os.path.splitext(item)[1]
                ext = ext.lower();
                ext = ext[1:]
                if CLEAN_TARGETS.count(ext)>0:
                   os.remove(filePath)
                   print "  Remove " + filePath

            #recursive clean
            elif os.path.isdir( filePath ):
                clean(filePath)

        #check if the directory is empty
        paths = os.listdir(dir)
        if len(paths)==0:
            dirNames = dir.split(os.path.sep)
            lastName = dirNames[len(dirNames)-1]
            lastName = lastName.lower()
            if lastName=='debug':
                os.rmdir(dir)
                print "  Remove " + dir
    except Exception, e:
        paths = []
        print e

# main body
dir = cur_file_dir()
confirm(dir)
clean(dir)
print "Done"
