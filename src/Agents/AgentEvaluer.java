package Agents;
import jade.wrapper.*;           
import jade.core.behaviours.CyclicBehaviour;             
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
public class AgentEvaluer extends Agent {
     private Evaluateur bbGui;
     int nb=0;
     protected void setup() {  
    bbGui = new Evaluateur(this);
  System.out.println("L'agent  :"+getAID().getName()+" est pret.");
    /////////////////////////////////////////////////
    addBehaviour(new CyclicBehaviour(this) {
 public void action() {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setContent("Bonjour1");
    msg.addReceiver(new AID("ag2", AID.ISLOCALNAME));    
    }

    });
 //////////////////////////////////////


//////////////////////////////////////////
    	     //////////////// regetrer /////////////////////       
    ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Evaluateur" );
        sd.setName( getLocalName() );
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
   try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
      /////////////////////////////////////////////////////
       

     //////////////////////////////////////////                                
    };
                            
    ////////////////////////////////////

                              /////////////////////////////////
                     ///////////////////////////////////////                                    
 ///////////////////////////////////                           
}