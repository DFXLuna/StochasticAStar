# Stochastic A*
An implementation of the A* algorithm in stochastic environment.

The agent files are stored in competition/noisy.

## To create your own agent
* Get a copy of the competition source from below
* Create a folder in competition
* Copy ch/idsia/ai/agents/SimpleAgent.Java to your new folder
* Rename the new SimpleAgent file and the class to your desired agent name
* Change the field Name to your agent class's name
* Edit ch/idsia/scenarios/Play.java
* To your imports, add
``` java
import competition.noisy.YourAgentFolder.YourAgent;
```
* Prior to the println's in main add
``` java
options.setAgent(new YourAgent());
```
* Run ch/idsia/scenarios/Play.java

## Some other stuff
* And example of the edits needed for play is available in this repo's ch/idsia/scnarios/Play.java
* The Agent has about 40ms to make a decision and give it back to the game before it will desync
* All options available from the command line can be manually activated in Play.main()

## Links
* [Unmodified Competition Source](http://julian.togelius.com/mariocompetition2009/marioai.zip)
* [Mario AI 2009 Website](http://julian.togelius.com/mariocompetition2009/)
* [All entries to the ICE-CIG and CIG 2009 Competitions](http://julian.togelius.com/mariocompetition2009/marioaiwithentrants.zip)
