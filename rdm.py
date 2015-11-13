#!/usr/bin/env python

import json

class RDMFile:
	
	def __init__(self):
		self.j = dict()

	def load(self, fname):
		fin = open(fname)
		self.j = json.load(fin)
		fin.close()
	
	def dump(self, fname, compressed=False):
		fout = open(fname, 'w+')
		if compressed:
			json.dump(self.j, fout, indent=None, separators=(',',':'))
		else:
			json.dump(self.j, fout, indent=4, separators=(',',': '))
		fout.flush()
		fout.close()

if __name__ == '__main__':
	rdm = RDMFile()
	rdm.load('map_examples/barrel.rdm')
	# ...
	rdm.dump('map_examples/barrel.yaml', False)
