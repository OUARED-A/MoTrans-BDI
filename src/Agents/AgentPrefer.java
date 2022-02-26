

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
public class AgentPrefer extends Agent {
     private Preference bbGui;
     int nb=0;
     protected void setup() {  
    bbGui = new Preference(this);
  System.out.println("L'agent  :"+getAID().getName()+" est pret.");
    /////////////////////////////////////////////////
    addBehaviour(new CyclicBehaviour(this) {
 public void action() {
 	
    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
	    if (msg != null) { 
				if ("Extraction  terminee".equalsIgnoreCase(msg.getContent())) {
			    System.out.println(getLocalName()+" recoit un message par   "+msg.getSender().getLocalName()); 
			    ACLMessage reply = msg.createReply();
			    reply.setContent("Reponse");
			    send(reply);
			    System.out.println(getLocalName()+" envoi la reponse au message envoye");
			    System.out.println("_________ fin de traitement du   message ______________" );
    
                            
    //Choix de mode de selection 
    // repartition des attributs 
                            
    ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
    msg1.setContent("Lancer la selection");
    
    msg1.addReceiver(new AID("Agent_SFH", AID.ISLOCALNAME));   
    
    send(msg1);
   // System.out.println(getLocalName());
   System.out.println();
 
    System.out.println(getLocalName()+" envoi le message a Agent FH ");
   
   
   
  // doWait(1000); // pour la synchronisation 
  // ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
  //  msg2.setContent("Lancer la selection");
    
  //  msg2.addReceiver(new AID("Agent_SFH", AID.ISLOCALNAME));   
    
 //  send(msg2);
 //  System.out.println(getLocalName());
 //  System.out.println();
 
 //   System.out.println(getLocalName()+" envoi le message a Agent FH ");
   
      
    
               System.out.println("_________ Fin du    message  ______________" );
             
    
			    
			    
			    
			    
				}}
				
				
				

							
				
    }

    });
 //////////////////////////////////////


//////////////////////////////////////////
    	     //////////////// regetrer /////////////////////       
    ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Preference" );
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