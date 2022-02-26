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
public class AgentSFH extends Agent {
     private SFH bbGui;
     int nb=0;
     protected void setup() {  
    bbGui = new SFH(this);
  //  bbGui.show();
  System.out.println("L'agent  :"+getAID().getName()+" est pret.");
   // bbGui.showMsg("L'agent  :"+getAID().getName()+" est pret.");
    /////////////////////////////////////////////////
    addBehaviour(new CyclicBehaviour(this) {
 public void action() {
  
     ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
	    if (msg != null) { 
                
				if ("Lancer la selection".equalsIgnoreCase(msg.getContent())) {
			    System.out.println(getLocalName()+" recoit un message par   "+msg.getSender().getLocalName()); 
			    ACLMessage reply = msg.createReply();
                            
			    reply.setContent("Réponse");
                       
			    send(reply);
			    System.out.println(getLocalName()+" envoi la reponse au message envoye");
			    System.out.println("_________ fin de traitement du   message ______________" );   
			    
  // if ("IJBSEUL".equalsIgnoreCase(msg.getContent())) {
  //  takeDown();   
  // }			    
			    
	
   
			    
			        ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
    msg1.setContent(" la séléction de schema de FH est terminee");
    
    msg1.addReceiver(new AID("Agent_SIJB", AID.ISLOCALNAME));   
    
    send(msg1);
   // System.out.println(getLocalName());
   System.out.println();
 
    System.out.println(getLocalName()+" envoi le message à Agent IJB ");
   
			    
			    
			     
    }
}
    
    }

    });
 //////////////////////////////////////

  
//////////////////////////////////////////
    	     //////////////// regetrer /////////////////////       
    ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Agent_SFH" );
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
     protected void takeDown() {        
        doDelete();          
        
                                             
        
      }
     
}