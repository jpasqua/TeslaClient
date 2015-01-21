#Tesla Model S Rest API

A Java implementation of the client side interface to the Tesla Model S API documented at:

+	[Tesla Model S REST API](http://docs.timdorr.apiary.io/)
+	[Tesla Model S Remote Access Protocol](http://tinyurl.com/mnjyhbb)

**Note:** This version of the library corresponds to the "owner" API from Tesla which came about in the same timeframe as version 6 of the car software. This version is completely incompatible with the old Tesla API and the old version of this library. If you want to continue to use the older library, please refer to tag 0.5.

This is unofficial documentation of the Tesla Model S REST API used by the iOS and Android apps. It features functionality to monitor and control the Model S remotely. The documents are updated as new information is found.

This software and documentation do not come from Tesla Motors Inc.

*Be careful* when using this software as it can lock and unlock your car as well as control various functions relating to the charging system, sun roof, lights, horn, and other subsystems of the car.

*Be careful* not to send your login and password to anyone other than Tesla or you are giving away the authentication details required to control your car.

#Disclaimer

Use these programs at your own risk. The authors do not guaranteed the proper functioning of these applications. This code attempts to use the same interfaces used by the official Tesla phone apps. However, it is possible that use of this code may cause unexpected damage for which nobody but you are responsible. Use of these functions can change the settings on your car and may have negative consequences such as (but not limited to) unlocking the doors, opening the sun roof, or reducing the available charge in the battery.

#Contributors
[Joe Pasqua](https://github.com/jpasqua)

#Preparing your build environment

This project assumes a directory structure that looks like this:

	Tesla					-- Overall container that may include other Tesla related projects
		TeslaClient			-- This project
			build
			src
	ThirdParty				-- A repository for third party library dependencies
		apache
			commons-codec-1.10
			commons-lang3-3.3.2
			commons-logging-1.1.3
		geocoder
		google-gson-2.2.4
		resty

The Tesla/TeslaClient directory corrsponds to this github project (TeslaClient.git). 

The following commands will create and populate the hierarchy. It assumes that:

+ <code>$ROOT</code>is where the Tesla and ThirdParty directories will reside

Be sure to either set these variables or adapt the commands below:

	cd $ROOT
	mkdir $ROOT/Tesla
    mkdir $ROOT/ThirdParty
    mkdir $ROOT/ThirdParty/apache
    mkdir $ROOT/ThirdParty/geocoder-java
    mkdir $ROOT/ThirdParty/resty

    # Download the TeslaClient repo
    cd $ROOT/Tesla
    git clone https://github.com/jpasqua/TeslaClient.git

	# Download the apache libraries
	cd $ROOT/ThirdParty/apache
	curl -s -O http://www.eng.lsu.edu/mirrors/apache/commons/codec/binaries/commons-codec-1.10-bin.zip
	unzip commons-codec-1.10-bin.zip
    rm commons-codec-1.10-bin.zip

	curl -s -O http://mirror.cc.columbia.edu/pub/software/apache//commons/lang/binaries/commons-lang3-3.3.2-bin.zip
	unzip commons-lang3-3.3.2-bin.zip
    rm commons-lang3-3.3.2-bin.zip

	curl -s -O http://apache.mirrors.hoobly.com//commons/logging/binaries/commons-logging-1.1.3-bin.zip
	unzip commons-logging-1.1.3-bin.zip
	rm commons-logging-1.1.3-bin.zip

	# Download the geocoder library
	cd $ROOT/ThirdParty/geocoder-java
	curl -s -O http://repo1.maven.org/maven2/com/google/code/geocoder-java/geocoder-java/0.15/geocoder-java-0.15.jar

	# Download the gson library
	cd $ROOT/ThirdParty
	curl -s -O http://google-gson.googlecode.com/files/google-gson-2.2.4-release.zip
	unzip google-gson-2.2.4-release.zip
	rm google-gson-2.2.4-release.zip

	# Download the resty library
    cd $ROOT/ThirdParty
	cd resty
	curl -s -O http://repo2.maven.org/maven2/us/monoid/web/resty/0.3.2/resty-0.3.2.jar

#Tests and Samples
There are two test programs included in the project: <code>BasicTest</code> and <code>Interactive</code>. The former simply runs through a sequence of functions in the client library to demonstrate that it is connecting and working. The second presents an interactive shell that allows the user to issue the various commands that are available through the client library.

To use either of these programs you must have active credentials for a Tesla vehicle that has remote access enabled. If you have more than one vehicle, you may select which vehicle to use in the Interactive program. BasicTest will always use the first vehicle returned by the Tesla portal.
