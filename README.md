# Stochastic A*
An implementation of the A* algorithm in a modified version of the Mario AI 2009 competition.

The agent files are stored in competition/noisy.

## How it works
The NoisyAgent runs a simulator alongside the actual game.
On every call to [NoisyAgent.getAction()](../master/competition/noisy/NoisyAgent.java#L67-L100):

### Without Markovian Noise
1. Update the simulator based on current observations and the previous action.
2. Check the accuracy of the current simulator state(It occasionally desyncs)
3. If there isn't a plan, call [NoisyAgent.Plan()](../master/competition/noisy/NoisyAgent.java#L103-L107) (This may or may not change)
4. [NoisyAgent.Plan()](../master/competition/noisy/NoisyAgent.java#L103-L107) calls [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan()
5. [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan() calls [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Search()
6. [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Search() backs up the current simulator state and runs A* through a set of generated states.
7. [AStarSimulator](../master/competition/noisy/AStarSimulator.java).getPlan() extracts the path from the result of the search and returns it back up to [NoisyAgent.Plan()](../master/competition/noisy/NoisyAgent.java#L103-L107)
8. After planning, [NoisyAgent.getAction()](../master/competition/noisy/NoisyAgent.java#L67-L100) pops the first action off of the plan and returns it unless noise is enabled.
9. If noise is enabled, [NoisyAgent.popAction()](../master/competition/noisy/NoisyAgent.java#L109-L120) first calls NoisyAgent.noise(). This applies one of three different kinds of noise depending on the kind of trial being run.
10. After noise is applied, the resulting action is returned.

### With Markovian noise
Almost the same as  without except for these changes.

* In addition to step one, the [Markov Chain](../master/competition/noisy/MarkovChain.java) is also updated based on the previous action.
* In step five, [AStarSimulator](../master/competition/noisy/AStarSimulator.java).msearch() is called instead. This search behaves exactly like [AStarSimulator](../master/competition/noisy/AStarSimulator.java).search() except for the the following changes:
 * A [Markov Chain](../master/competition/noisy/MarkovChain.java) is passed as a parameter.
 * [AStarSimulator](../master/competition/noisy/AStarSimulator.java).mNodes are created rather than regular nodes. The only difference is that these nodes hold and handle probabilities given to them by a [Markov Chain](../master/competition/noisy/MarkovChain.java)
 * [AStarSimulator](../master/competition/noisy/AStarSimulator.java).mNodes take the chance of noise into account in their cost function.

## Implementation Details
The agent consists of four parts: [NoisyAgent](../master/competition/noisy/NoisyAgent.java), [AStarSimulator](../master/competition/noisy/AStarSimulator.java), [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node and [MarkovChain](../master/competition/noisy/MarkovChain.java)

### [AStarSimulator](../master/competition/noisy/AStarSimulator.java)
* The simulator is based on [Robin Baumgarten's simulator](https://github.com/RobinB/mario-astar-robinbaumgarten). Everything was stripped except for the parts of his code which were either modified pieces of engine code or utility functions. Most magic numbers that pertain to the engine were also taken.
* The simulator acts as a front end to a copy of [ch.idsia.mario.engine.LevelScene](../master/ch/idsia/mario/engine/LevelScene.java). The copy is stored in [competition.noisy.LevelScene](../master/competition/noisy/LevelScene.java). This version has all of the rendering code removed. Other than that it's almost identical to the engine's LevelScene and behaves the same way.
* The current "real" state is held in [AStarSimulator](../master/competition/noisy/AStarSimulator.java).levelScene. [AStarSimulator](../master/competition/noisy/AStarSimulator.java).workScene is used to hold modifications made by [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren() during search. 
* The simulator implements time sensitive early exit, so line by line debugging won't work unless it is disabled.
* Early exit is implemented in [AStarSimulator](../master/competition/noisy/AStarSimulator.java).search(). The time limit is 9ms (9000000ns). The time budget can be experimented with by changing [AStarSimulator](../master/competition/noisy/AStarSimulator.java).timeBudget. A small portion of the time budget is reserved for post search processing, this can be changed in [AStarSimulator](../master/competition/noisy/AStarSimulator.java).postSearchReserve. The default is 1ms(1000000ns). 
* There is a system to pass simulator state information forward to future calls of [AStarSimulator](../master/competition/noisy/AStarSimulator.java).search(). The simulator's state is kept intact unless either [NoisyAgent.reset()](../master/competition/noisy/NoisyAgent.java#L51-L58) or [AStarSimulator](../master/competition/noisy/AStarSimulator.java).simNull() is called.
* To pass a state forward, just comment out ``sim.simNull();`` in [NoisyAgent.Plan()](../master/competition/noisy/NoisyAgent.java#L103-L107) and [AStarSimulator](../master/competition/noisy/AStarSimulator.java).cloneState() the workScene into simState after the search. You should also specify some criteria for dealing with that state, otherwise the simulator will ignore state updates and desync.
* This allowed breaking the search up in to pieces. However, this was a little too heavy to do quickly. The extra overhead of recalling each function and recreating nodes is too much.
* [AStarSimulator](../master/competition/noisy/AStarSimulator.java).msearch() and  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).mNode just bake a probability into the cost of a node.
* Because learning isn't employed for the Markov Chain, the only permenant observation that search gives to the chain is the prior. 
* Because the Markov Chain is preserved between calls to  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).msearch(),
the latest real position is always the prior.

### [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node
* Each state is created as a child of a parent state by applying each valid move to the parent state and encapsulating each result in an [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node. The node holds the entire state including all Mario data, level data and enemy data.
* Nodes are responsible for generating their own children so calling code doesn't have to deal with node's auxillary functions. This is done in [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren()
* Move generating code is also implemented in node because the only function that calls it is [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren()
* The cost and heuristic for A* are both implemented in the node. This makes accessing node members much cleaner.
* mNode overrides  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.cost and  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren().
 * [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.cost is overriden to bake the probability of noise into the node's cost.
 *  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren() is overriden to create  [AStarSimulator](../master/competition/noisy/AStarSimulator.java).mNodes when searching.

### [NoisyAgent](../master/competition/noisy/NoisyAgent.java)
* If noise is enabled, it's applied to actions during [NoisyAgent.popAction()](../master/competition/noisy/NoisyAgent.java#L109-L120). 
* The three types of noise that this implements are cancellation, randomization and Markovian noise.
* Cancellation ignores an action.
* Randomization ignores an action and sets a new random action.
* Markovian noise bases the chance to change an action on the previous state. This makes it easier to implement and test a Markov Chain model for estimating noise.
* Strategy selection is implemented as a constant [NoisyAgent](../master/competition/noisy/NoisyAgent.java).STRAT.
 * If Reactive is chosen, the agent will replan after each frame with noise. See "A Note on Reactive Strategies".
 * If Proactive is chose, the agent will employ a [MarkovChain](../master/competition/noisy/MarkovChain.java) to estimate the probability of noise at each step.
 * Both can be employed simultaneously

### [MarkovChain](../master/competition/noisy/MarkovChain.java)
* This is just the most basic implementation of a Markov Chain. It implements state estimation and prediction.
* Backup and restore can be used to simulate what would happen if a particular observation is found. This is more accurate than using predict. Since states are being simulated, the program takes advantage of this during [AStarSimulator](../master/competition/noisy/AStarSimulator.java).Node.genChildren()

### Misc
* A Verbosity switch (VERBOSE) is set in the [Verbose](../master/competition/noisy/Verbose.java) interface. Both [NoisyAgent](../master/competition/noisy/NoisyAgent.java) and [AStarSimulator](../master/competition/noisy/AStarSimulator.java) use this switch to determine whether or not to print diagnostic output to stdout.

###  Note on Reactive Strategies.
The reactive strategy is probably the best but also the most expensive as it requires a full replan whenever noise occurs. As noise approaches 100%, the reactive strategy will approach replanning on every frame. This means the chance of a desync increases drastically.

## To create your own agent
* Get a copy of the [unmodified competition source](http://julian.togelius.com/mariocompetition2009/marioai.zip).
* Create a folder for your agent code in competition/.
* Copy ch/idsia/ai/agents/SimpleAgent.Java to competition/yourNewFolder.
* Rename the new SimpleAgent file and the class to your desired agent name.
* Change the field Name to your agent class's name.
* Edit ch/idsia/scenarios/Play.java.
* To your imports, add
``` java
import competition.noisy.YourAgentFolder.YourAgent;
```
* Prior to the println's in main add
``` java
options.setAgent(new YourAgent());
```
* Run ch/idsia/scenarios/Play.java.
* Similiar step can be taken to use the learning interface available in [ch.idsia.scenarios.Evolve.java](../master/ch/idsia/scenarios/Evolve/java).

## Some other stuff
* An example of the edits needed for play is available in [Play.java](../master/ch/idsia/scenarios/Play.java).
* Creating actions is easy; [AStarSimulator](../master/competition/noisy/AStarSimulator.java).createAction() shows how.
* The agent can access information about the world from the Environment interface that is passed in to the object. [ch.idsia.mario.environments.Environment.java](../master/ch/idsia/mario/environments/Environment.java) contains relevant information.
* The Agent has approximately 40ms to make a decision and give it back to the game before it will desync. This may or may not be frame rate dependent.
* All options available from the command line can be manually activated in Play.main().


## Links
* [Unmodified Competition Source](http://julian.togelius.com/mariocompetition2009/marioai.zip)
* [Mario AI 2009 Website](http://julian.togelius.com/mariocompetition2009/)
* [All entries to the ICE-CIG and CIG 2009 Competitions](http://julian.togelius.com/mariocompetition2009/marioaiwithentrants.zip)
