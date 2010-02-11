
CLDCAPI=cldcapi11.jar
MIDPAPI=midpapi20.jar

# Java installation
JAVA=/usr/bin/java
JAVAC=/usr/bin/javac
JAR=/usr/bin/jar

# Sun WTK installation
WTK=/opt/WTK2.5.2
WTKVERSION=2.5.2
WTKDEVICE=DefaultColorPhone
JAVAFILESYS=$(HOME)/j2mewtk/$(WTKVERSION)/appdb/$(WTKDEVICE)/filesystem

# Mnemogogo library
MNEMOGOGO=$(HOME)/.mnemosyne/plugins/mnemogogo/mnemogogo-j2me-1.0.1.jar

# Microemulator installation
MICROEMU=/opt/microemulator

# Misc
XSLTPROC=xsltproc

mnemojojo: bin/mnemojojo.jar bin/mnemojojo.jad
	@echo --Todo items remaining in source:
	-@grep XXX src/mnemojojo/*.java
	-@grep TODO src/mnemojojo/*.java

zip: bin/mnemojojo.jar bin/mnemojojo.jad version
	zip bin/mnemojojo-`cat version`.zip \
	  --junk-paths --quiet \
	  bin/mnemojojo.jar \
	  bin/mnemojojo.jad

emulator: bin/mnemojojo.jar bin/mnemojojo.jad filesystem
	$(WTK)/bin/emulator \
	  -Xdevice:$(WTKDEVICE) \
	  -Xdomain:maximum \
	  -Xheapsize:1024k \
	  -Xdescriptor:bin/mnemojojo.jad

filesystem:
	ln -s $(JAVAFILESYS) filesystem

microemulator: bin/mnemojojo.jad bin/mnemojojo.jar filesystem
	$(JAVA) -classpath $(MICROEMU)/microemulator.jar:$(MICROEMU)/lib/microemu-jsr-75.jar:$(MICROEMU)/devices/microemu-device-large.jar \
	  org.microemu.app.Main \
	  --impl org.microemu.cldc.file.FileSystem \
	  --device org/microemu/device/large/device.xml \
	  bin/mnemojojo.jad

microemulatorconfig:
	cp $(HOME)/.microemulator/config2.xml $(HOME)/.microemulator/config2.xml.bkp
	$(XSLTPROC) --novalid --stringparam fsRoot "$(JAVAFILESYS)" \
	    ./util/microemu_config.xml \
	    $(HOME)/.microemulator/config2.xml.bkp \
	    > $(HOME)/.microemulator/config2.xml

compile: sources tmpclasses tmpclasses/mnemogogo
	$(JAVAC) \
	  -source 1.4 -target 1.4 \
	  -bootclasspath $(WTK)/lib/$(CLDCAPI):$(WTK)/lib/$(MIDPAPI):$(WTK)/lib/jsr75.jar \
	  -classpath tmpclasses \
	  -d tmpclasses @sources

preverify: compile classes tmpclasses/mnemogogo
	$(WTK)/bin/preverify \
	  -classpath tmpclasses:$(WTK)/lib/$(CLDCAPI):$(WTK)/lib/$(MIDPAPI):$(WTK)/lib/jsr75.jar \
	  -d classes tmpclasses

tmpclasses/mnemogogo: $(MNEMOGOGO)
	mkdir -p tmpclasses
	cd tmpclasses && jar xf $(MNEMOGOGO) mnemogogo

bin/mnemojojo.jar: preverify bin/MANIFEST.MF bin tmpclasses/mnemogogo
	$(JAR) cfm bin/mnemojojo.jar bin/MANIFEST.MF -C classes . -C res .

bin/mnemojojo.jad: bin/mnemojojo.jar version bin
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
	find -L ./src -regex '.*.java$$' -and -not -regex '.*Android.java$$' > sources

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

cleanall: clean
	-@rm sources

