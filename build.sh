#!/bin/bash

# Create build directory
mkdir -p build

# Compile the source files
javac -d build src/main/java/com/resourcegame/**/*.java

# Run the game
java -cp build com.resourcegame.core.Game
