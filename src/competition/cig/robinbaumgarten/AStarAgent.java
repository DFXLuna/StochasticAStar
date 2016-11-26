package competition.cig.robinbaumgarten;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

import competition.cig.robinbaumgarten.astar.AStarSimulator;
import competition.cig.robinbaumgarten.astar.sprites.Mario;

public class AStarAgent implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "RobinBaumgarten_AStarAgent";
    private AStarSimulator sim;
    private int tickCounter = 0;
    private float lastX = 0;
    private float lastY = 0;
	int errCount = 0;
	AStarAgent errAgent;
    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        sim = new AStarSimulator();
    }
    
    public void printLevel(byte[][] levelScene)
    {
    	for (int i = 0; i < levelScene.length; i++)
    	{
    		for (int j = 0; j < levelScene[i].length; j++)
    		{
    			System.out.print(levelScene[i][j]+"\t");
    		}
    		System.out.println("");
    	}
    }

    public boolean[] getAction(Environment observation)
    {
    	long startTime = System.currentTimeMillis();
    	tickCounter++;
    	String s = "Fire";
    	if (!sim.levelScene.mario.fire)
    		s = "Large";
    	if (!sim.levelScene.mario.large)
    		s = "Small";
    	if (sim.levelScene.verbose > 0) System.out.println("Next action! Tick " + tickCounter + " Simulated Mariosize: " + s);

    	boolean[] ac = new boolean[5];
    	ac[Mario.KEY_RIGHT] = true;
    	ac[Mario.KEY_SPEED] = true;
    	
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
 
        sim.advanceStep(action);   
        
		float[] f = observation.getMarioFloatPos();
		if (sim.levelScene.mario.x != f[0] || sim.levelScene.mario.y != f[1])
		{
			if (f[0] == lastX && f[1] == lastY){ return ac; }
			sim.levelScene.mario.x = f[0];
			sim.levelScene.mario.xa = (f[0] - lastX) *0.89f;
			if (Math.abs(sim.levelScene.mario.y - f[1]) > 0.1f)
				sim.levelScene.mario.ya = (f[1] - lastY) * 0.85f;// + 3f;

			sim.levelScene.mario.y = f[1];
			errCount++;
			System.out.println(errCount);
		}
		sim.setLevelPart(scene, enemies);
        
		lastX = f[0];
		lastY = f[1];

        action = sim.optimise();
        
        sim.timeBudget += 39 - (int)(System.currentTimeMillis() - startTime);
        return action;
    }

    public AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() 
    {        
    	return name;    
    }

    public void setName(String Name) 
    { 
    	this.name = Name;    
    }
}
