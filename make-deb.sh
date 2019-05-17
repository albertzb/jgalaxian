#!/bin/bash

mvn package && cp target/jgalaxian-1.0-SNAPSHOT.jar ./deb_install_files/usr/lib/. && dpkg-buildpackage

