#!/bin/sh
# ==========================================================================
#     jcshell.sh
#     This file launches the NXP JCShell for command line usage
# ==========================================================================

# --------------------------------------------------------------------------
# Check for java
# --------------------------------------------------------------------------
if [ "$JAVA_HOME" != "" ]; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVA="$JAVA_HOME/bin/java"
    else
        echo "ERROR : JAVA_HOME not properly defined. Could not find java."
    exit
    fi
else
    JAVA="$(which java)"
    if [ "$JAVA" = "" ]; then
        echo "ERROR : Could not find java in PATH. Please define JAVA_HOME."
        exit
    fi
fi

# --------------------------------------------------------------------------
# Extract jcshell absolute path no matter from where it's called
# --------------------------------------------------------------------------
OSNAME=`uname`
#JAVA32=""
if [ $OSNAME = "Darwin" ]; then # MacOS 
    JCSHELL_PATH=$(cd "$(dirname "$0")"; pwd)
    #JAVA32="-d32"
    #if [ "$isOSX" != "" -a "$HOSTTYPE" = "x86_64" -a "$check64" != "" ]; then
    #    JAVA32='-d32'
    #fi
else
	if [ $OSNAME = "Linux" ]; then # Linux
	    JCSHELL_PATH=$(cd "$(dirname "$0")"; pwd)
	else
        JCSHELL_PATH="$(dirname "$(readlink -f "$0")")"
    #JAVA32=""
	fi
fi

# --------------------------------------------------------------------------
# Include the JAR files from lib folder in CLASSPATH for JVM
# --------------------------------------------------------------------------
CLASSPATH_JCSHELL=""
for entry in "$JCSHELL_PATH/lib"/*
do
CLASSPATH_JCSHELL="$CLASSPATH_JCSHELL:$entry"
done
CLASSPATH_JCSHELL="$CLASSPATH_JCSHELL_LOCAL_EXTENSION:$CLASSPATH_JCSHELL:$entry"
if [ "$NUT_HOME" != "" ]; then
	CLASSPATH_JCSHELL="$CLASSPATH_JCSHELL:$NUT_HOME/nut.jar"
fi

export PATH=$PATH:$JCSHELL_PATH

# --------------------------------------------------------------------------
# Start the JVM with the user specified heap memory
# --------------------------------------------------------------------------
i=0
MEM="-m"
HEAP="--heapmem"
X="Xmx"
M="M"
HEAPMEM=1024
HEAPSIZE=-$X$HEAPMEM$M
for VAR in "$@"
do
	if [ $i -eq 1 ] 
	then 
		HEAPSIZE=-$X$VAR$M
		i=2
	elif [ $VAR = $MEM ] || [ $VAR = $HEAP ]
	then
		i=1
	fi		
done

# --------------------------------------------------------------------------
# Launch the JVM with jcshell as main application and pass in the command
# line from the call to this script file.
# --------------------------------------------------------------------------
echo
echo Welcome to NXP JCShell
echo "(c) 2020 NXP Semiconductors"
echo ------------------------------------------------------------------------------
echo
cd "$JCSHELL_PATH"
#"$JAVA" $JAVA32 -cp "$CLASSPATH_JCSHELL" com.nxp.id.jc.tools.JCShell $@
"$JAVA" $HEAPSIZE -cp "$CLASSPATH_JCSHELL" com.nxp.id.jc.tools.JCShell "$@"
