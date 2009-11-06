
JAVAC=/usr/bin/javac
JAR=/usr/bin/jar
WTK=/opt/WTK2.5.2

CLDCAPI=$(WTK)/lib/cldcapi11.jar
MIDPAPI=$(WTK)/lib/midpapi20.jar
JSR75=$(WTK)/lib/jsr75.jar

mnemojojo: bin/mnemojojo.jar bin/mnemojojo.jad
	# TODO: add zip file

emulator: bin/mnemojojo.jar bin/mnemojojo.jad
	$(WTK)/bin/emulator \
	  -Xverbose:all \
	  -Xdevice:DefaultColorPhone \
	  -Xheapsize:500k \
	  -Xdescriptor:bin/mnemojojo.jad

compile: sources tmpclasses
	$(JAVAC) \
	  -source 1.4 -target 1.4 \
	  -bootclasspath $(CLDCAPI):$(MIDPAPI):$(JSR75) \
	  -d tmpclasses @sources

preverify: compile classes
	$(WTK)/bin/preverify \
	  -classpath $(CLDCAPI):$(MIDPAPI):$(JSR75) \
	  -d classes tmpclasses

bin/mnemojojo.jar: preverify bin/MANIFEST.MF bin
	$(JAR) cfm bin/mnemojojo.jar bin/MANIFEST.MF -C classes . -C res .

bin/mnemojojo.jad: bin/mnemojojo.jar bin
	@echo "MIDlet-1: Mnemojojo, mnemosyne.png, mnemojojo.FireMIDlet" > bin/mnemojojo.jad
	@echo "MIDlet-Icon: mnemosyne.png" >> bin/mnemojojo.jad
	@echo "MIDlet-Jar-Size: `ls -l bin/mnemojojo.jar | cut -d ' ' -f 5`" >> bin/mnemojojo.jad
	@echo "MIDlet-Jar-URL: mnemojojo.jar" >> bin/mnemojojo.jad
	@echo "MIDlet-Name: Mnemojojo" >> bin/mnemojojo.jad
	@echo "MIDlet-Permissions: javax.microedition.io.Connector.file.read,\
	javax.microedition.io.Connector.file.write" >> bin/mnemojojo.jad
	@echo "MIDlet-Vendor: Timothy Bourke" >> bin/mnemojojo.jad
	@echo "MIDlet-Version: `cat version`" >> bin/mnemojojo.jad
	@echo "MicroEdition-Configuration: CLDC-1.1" >> bin/mnemojojo.jad
	@echo "MicroEdition-Profile: MIDP-2.0" >> bin/mnemojojo.jad

version: src/mnemojojo/Core.java
	sed -ne 's/.*versionInfo *= *"Mnemojojo \([0-9.]*\)".*/\1/p' src/mnemojojo/Core.java \
		> version

bin/MANIFEST.MF: version bin
	@echo "MIDlet-1: Mnemojojo, mnemosyne.png, mnemojojo.FireMIDlet" > bin/MANIFEST.MF
	@echo "MIDlet-Name: Mnemojojo" >> bin/MANIFEST.MF
	@echo "MIDlet-Permissions: javax.microedition.io.Connector.file.read,\
	javax.microedition.io.Connector.file.write" >> bin/MANIFEST.MF
	@echo "MIDlet-Vendor: Timothy Bourke" >> bin/MANIFEST.MF
	@echo "MIDlet-Version: `cat version`" >> bin/MANIFEST.MF
	@echo "MicroEdition-Configuration: CLDC-1.1" >> bin/MANIFEST.MF
	@echo "MicroEdition-Profile: MIDP-2.0" >> bin/MANIFEST.MF

sources:
	find -L ./src -regex '.*\.java$$' > sources

tmpclasses:
	mkdir tmpclasses

classes:
	mkdir classes

bin:
	mkdir bin

clean:
	-@rm version sources 2>/dev/null || true
	-@rm bin/MANIFEST.MF bin/mnemojojo.jar bin/mnemojojo.jad 2>/dev/null || true
	-@rmdir bin 2>/dev/null || true
	-@find tmpclasses -name '*.class' -delete          2>/dev/null || true
	-@find tmpclasses -depth -type d -exec rmdir {} \; 2>/dev/null || true
	-@find classes -name '*.class' -delete             2>/dev/null || true
	-@find classes -depth -type d -exec rmdir {} \;    2>/dev/null || true

