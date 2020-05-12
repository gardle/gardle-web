#!/bin/bash

languages=(en)
google_translate_api="" # add your own API Key

for lang in $languages; do
		mkdir $lang
		for file in de/*; do
		  filebase=`basename $file`
		  node translate-json $google_translate_api $file $lang "$lang/${filebase}"; 
		done
done
