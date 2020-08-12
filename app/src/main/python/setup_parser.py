#!/usr/bin/env python3
# coding: utf8


from android.os import Environment

import iodevices
import writers
import parsers
import subprocess
import string
import subprocess
import threading
import time
import os, sys, re, importlib
import argparse
import signal
import struct
import util
import faulthandler
import logging
import os.path
from os import path

GSMTAP_PORT = 4729
IP_OVER_UDP_PORT = 47290


def create_directory():
    try:
        print(subprocess.check_output(['su', '-c', 'mkdir /data/media/0/MobileSentinel']))

    except Exception as e:
        print(e)

    try:
        print(subprocess.check_output(['su', '-c', 'chmod 777 /data/media/0/MobileSentinel']))
    except Exception as e:
        print(e)


def start_logging(filename):
    # Check if log folder exists and has permissions
    try:
        print(subprocess.check_output(['su', '-c', 'mkdir /data/media/0/MobileSentinel']))

    except Exception as e:
        print(e)

    try:
        print(subprocess.check_output(['su', '-c', 'chmod 777 /data/media/0/MobileSentinel']))
    except Exception as e:
        print(e)

    # Initiate mdlog and pass config files
    try:
        print(
            subprocess.check_output(['su', '-c', 'mkdir /data/media/0/MobileSentinel/' + filename]))
        print(subprocess.check_output(
            ['su', '-c', 'chmod 777 /data/media/0/MobileSentinel/' + filename]))
        subprocess.Popen(['su', '-c',
                          'diag_mdlog -s 90000 -f /data/media/0/MobileSentinel/rrc_filter_diag -o /data/media/0/MobileSentinel/' + filename])
        print("Mdlog is running")
    except Exception as e:
        print(e)


def stop_logging():
    # subprocess.Popen(['su','-c','diag_mdlog -k'])
    print(subprocess.check_output(['su', '-c', 'diag_mdlog -k']))
    #print("Mdlog is stopped")


def initiate_parsing(packet_list, dump_directory, dump_filename, detection_view=None,
                     isVulnerable=None):
    # Setup dump parser modules
    my_parser = parsers.QualcommParser(packet_list, detection_view, isVulnerable)
    d = str(Environment.getExternalStorageDirectory());
    filepath = os.path.join(d, 'MobileSentinel/' + dump_directory + '/' + dump_filename)
    try:
        io_device = iodevices.FileIO([str(filepath)])
        my_parser.set_io_device(io_device)
    except Exception as e:
        print(e)
    writer = writers.PcapWriter(dump_directory, GSMTAP_PORT, IP_OVER_UDP_PORT)
    my_parser.set_writer(writer)
    my_parser.read_dump()

