#!/usr/bin/env bash

# nothing fancy here... we need python 3 and pip in java VMs in Travis comes with python 2.7...
# installing pip3 didn't work, yeah, we could force it, but https://www.youtube.com/watch?v=waEC-8GFTP4
wget --quiet --output-document Miniconda3-latest-Linux-x86_64.sh https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh && chmod +x Miniconda3-latest-Linux-x86_64.sh && ./Miniconda3-latest-Linux-x86_64.sh -b -f
$HOME/miniconda3/bin/conda update --quiet --yes -n base -c defaults conda
$HOME/miniconda3/bin/conda create --quiet --yes --channel conda-forge --name qbic-docs-build python=3 pygithub