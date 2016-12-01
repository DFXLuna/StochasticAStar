package competition.noisy;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.Agent;
import competition.noisy.AStarSimulator.*;
import java.util.ArrayList;
import java.util.Random;
/**
 * User: Matt Grant 
 * Date: 5 Noc 2016
 * Package: competition.noisy
 */
// Some people write disgusting code. We call those people Java Devs
// I thought python was atrocious but Java is clearly on another level
// Have you ever looked at the interface for 
// implementing a Priority queue in Java? It's disgusting


public class NoisyAgent implements Agent{

	protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "NoisyAgent";
    private AStarSimulator sim;
    private ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    public boolean requirePlan = true;
    private boolean[] prevAction = null;
    private static boolean VERBOSE = false;
    // 0 = No noise
    // 1 = Cancellation
    // 2 = Randomization
    // 3 = Markovian
    private static int NOISE = 0;
    
    public void reset(){
        requirePlan = true;
        action = new boolean[Environment.numberOfButtons];
        sim = new AStarSimulator();
    }
    // Interestingly, the program has a memory leak somewhere.
    // Simulation becomes inaccurate when we take more than some amount of time to plan
    
	// TODO
    // Determine whether always replan or sparse plan is better
    // Noise types: Cancel, Random, Markovian
    // Deal with noise
    // Do stats
    // Estimate Trajectory (Maybe)
    // Create project site to hold data
    // Update README.MD to link to specific lines of code
    public boolean[] getAction(Environment observation){
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	if(prevAction != null){
    		sim.advanceStep(prevAction);
    	}
    	sim.setLevelPart(scene, enemies);
    	
    	float[] m = observation.getMarioFloatPos();
    	if (sim.levelScene.mario.x - m[0] > 0.1f || 
    	sim.levelScene.mario.y - m[1] > 0.1f ){
    		if(VERBOSE){
    			System.out.println("Inaccuracy in simulator position");
    			System.out.println("Expected XY: " + sim.levelScene.mario.x + " " + sim.levelScene.mario.y);
    			System.out.println("Actual XY:   " + m[0] + " " + m[1]);
    		}
    		sim.levelScene.mario.x = m[0];
    		sim.levelScene.mario.y = m[1];
    	}
    	// We only want to replan if the last plan has expired
    	if(true){
    		plan();
    		requirePlan = false;
    	}
        return popAction();
    }
    private void plan(){
    	plan.clear();
    	plan.addAll(sim.getPlan());
    	sim.simNull();
    }
    
    private boolean[] popAction(){
    	if(plan.size() == 1){
    		requirePlan = true;
    	}
		boolean[] toReturn = plan.get(0);
		prevAction = toReturn;
		plan.remove(0);
		if(NOISE > 0){
			noise(toReturn);
		}
		return toReturn;
    }
    
    private void noise(boolean[] action){
    	Random rand = new Random();
    	
    	int n = rand.nextInt(101) + 1;
    	if(n > 90){
    		if(NOISE == 1){
    			// Cancellation
        		for(boolean b : action){
        			if(b == true){
        				b = false;
        			}
        		}
    		}
    		else if(NOISE == 2){
    			// Randomization
        		for(boolean b : action){
        			int r = rand.nextInt(2);
        			if(r == 1){
        				b = true;
        			}
        			else{
        				b = false;
        			}
        		}
    		}
    		else if(NOISE == 3){
    			// Markovian noise
    			mNoise(action);
    		}
    	}
    }
    
    private boolean[] mNoise(boolean[] action){
    	
    	return action;
    }
    
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {        
    	return name;
    }

    public void setName(String Name) {
    	this.name = Name;    
    }
    

}
