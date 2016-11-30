# Stochastic A*
An implementation of the A* algorithm in stochastic environment.

The agent files are stored in competition/noisy.

## How it works
The NoisyAgent runs a simulator alongside the actual game.
On every call to NoisyAgent.getAction():
* Update the simulator based on current observations and the previous action.
* Check the accuracy of the current simulator state(It occasionally desyncs)
* If there isn't a plan, call [NoisyAgent](../master/competition/noisy/NoisyAgent.java).Plan() (This may or may not change)
* [NoisyAgent](../master/competition/noisy/NoisyAgent.java).Plan() calls [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan()
* [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan() calls [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Search()
* [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Search() backs up the current simulator state and runs A* through a set of generated states.
* [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan() extracts the path from the result of the search and returns it back up to [NoisyAgent](../master/competition/noisy/NoisyAgent.java).Plan()
* After planning, [NoisyAgent](../master/competition/noisy/NoisyAgent.java).getAction() pops the first action off of the plan and returns it unless noise is enabled.
* If noise is enabled, NoisyAgent.popAction() first calls NoisyAgent.noise(). This applies one of three different kinds of noise depending on the kind of trial being run.
* After noise is applied, the resulting action is returned.

## Implementation Details
### [AStarSimulator](../master/competition/noisy/AStarSimulator.java)
* The simulator is based on (Robin Baumgarten's simulator)[https://github.com/RobinB/mario-astar-robinbaumgarten]. Everything was stripped except for the parts of his code which were either modified pieces of engine code or utility functions.
* The simulator works by keeping its own version of [ch.idsia.mario.engine.LevelScene](../master/ch/idsia/mario/engine/LevelScene.java). This version has all of the rendering code removed. Other than that it's almost identical to the engine's LevelScene 
* There is a system to pass simulator state information forward to future calls of [AStarSimulator](../master/competition/noisy/AStarSimulator.java).search(). This allowed breaking the search up in to pieces. However, this was a little too heavy to do quickly. The extra overhead of recalling each function and recreating nodes is too much.

### [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node
* Each state is created as a child of a parent state by applying each valid move to the parent state and encapsulating each result in an [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node. The node holds the entire state including all Mario data, level data and enemy data.
* Nodes are responsible for generating their own children so calling code doesn't have to deal with node's auxillary functions. This is done in [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren()
* Move generating code is also implemented in node because the only function that calls it is [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren()
* The cost and heuristic for A* are both implemented in the node. This makes accessing node members much cleaner.

### [NoisyAgent](../master/competition/noisy/NoisyAgent.java)
* If noise is enabled, it's applied to actions during [NoisyAgent](../master/competition/noisy/NoisyAgent.java).popAction(). Because the action is actually changed, nothing special needs to be done when advancing the simulator.



## To create your own agent
* Get a copy of the [unmodified competition source](http://julian.togelius.com/mariocompetition2009/marioai.zip)
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
* An example of the edits needed for play is available in [Play.java](../master/ch/idsia/scenarios/Play.java)
* The Agent has about 40ms to make a decision and give it back to the game before it will desync
* All options available from the command line can be manually activated in Play.main()
* Creating actions is easy; [AStarSimulator](../master/competition/noisy/AStarSimulator.java).createAction() shows you how

## Links
* [Unmodified Competition Source](http://julian.togelius.com/mariocompetition2009/marioai.zip)
* [Mario AI 2009 Website](http://julian.togelius.com/mariocompetition2009/)
* [All entries to the ICE-CIG and CIG 2009 Competitions](http://julian.togelius.com/mariocompetition2009/marioaiwithentrants.zip)
