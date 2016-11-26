package competition.noisy;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.Agent;
import competition.noisy.AStarSimulator.*;
import java.util.ArrayList;
/**
 * User: Matt Grant 
 * Date: 11/5/2016
 * Package: competition.noisy
 */
// Some people write disgusting code. We call those people Java Devs
// I thought python was atrocious but Java is clearly on another level
// Have you ever looked at the interface for 
// implementing a Priority queue in Java? It's disgusting


public class NoisyAgent implements Agent{

    protected String name = "NoisyAgent";
    private AStarSimulator sim;
    private ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    public boolean requirePlan = true;
    private boolean[] prevAction = null;
    private int repeat = 2;
    private int error = 0;
    

    public void reset(){
    	//boolean action[] = {false, true, false, true, true};
        //plan.add(action);
        requirePlan = true;
        error = 0;
        sim = new AStarSimulator();
    }

	// TODO
    // Jumps are still weird
    // Add noise
	// Check for inaccuracy in simulation
    // Possibly budget time
    public boolean[] getAction(Environment observation){
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	// Advance the sim based on last action.
    	float[] m = observation.getMarioFloatPos();
    	if(prevAction != null){
    		sim.advanceStep(prevAction);
    	}
    	sim.setLevelPart(scene, enemies);
    	

    	if (sim.levelScene.mario.x != m[0] || sim.levelScene.mario.y != m[1] - 3){
    		System.out.println("Inaccuracy in simulator position");
    		error++;
    		if(error > 5){
    			sim.levelScene.mario.x = m[0];
    			sim.levelScene.mario.y = m[1] - 3;
    			requirePlan = true;
    		}

    	}
    	// We only want to replan if the last plan has expired
    	if(requirePlan){
    		//reset();
    		plan();
    		requirePlan = false;
    	}
        return popAction();
    }
    private void plan(){
    	plan.clear();
    	for(int i = 0; i < repeat; i++){
    		plan.addAll(sim.getPlan());
    	}
    	sim.simNull();
    }
    
    public boolean[] popAction(){
    	if(plan.size() == 1){
    		requirePlan = true;
    	}
		boolean[] toReturn = plan.get(0);
		prevAction = toReturn;
		plan.remove(0);
		return toReturn;
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
