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

import java.io.IOException;
import java.lang.String;


public class AgentExtrac extends Agent {
  
    
    
  // lancer agent 
    
    public boolean lancerAgent(String machine){
     
        String jadeURL = "C:\\JADE";
        if (machine.equals("localhost")) { 
            try { 
                Runtime.getRuntime().exec("cmd /c start java -classpath " +   " jade.Boot -gui "); 
                
                Runtime.getRuntime().exec("cmd /c start " + jadeURL + "java jade.Boot –gui AgentExtrac");
                return true;
            } 
            catch (IOException ex) { 
                 return false;
            }
       
       
    }
        else 
        return true;
    } 
    
    
//
    
    
private Extracteur bbGui1;
     protected void setup() {  
  bbGui1 = new Extracteur(this);
  System.out.println("L'agent  :"+getAID().getName()+" est pret.");
    /////////////////////////////////////////////////  
  
    addBehaviour(new CyclicBehaviour(this) {
        Princ p1 =new Princ();
       
 public void action() {
     if (bbGui1.drap && p1.drap1==true){
        
    ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
    msg1.setContent("Extraction  terminee");
    
    msg1.addReceiver(new AID("Agent_Prefer", AID.ISLOCALNAME));   
     
    send(msg1);
   // System.out.println(getLocalName());
   System.out.println();
   
   System.out.println("************************Journal de communication************************");
   System.out.println("Extraction terminée");
   System.out.println(getLocalName()+" envoi le message a Agent Preference ");
    
   System.out.println("_________ Fin du    message  ______________" );
              bbGui1.drap=false;
              
                     
              
    //}
    ACLMessage msgr = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    if (msgr != null){
	    if ("Reponse".equalsIgnoreCase(msgr.getContent())) 
	        {System.out.println(getLocalName()+" recoit la reponse de    "+msgr.getSender().getLocalName()+"   Merci......");
	        System.out.println("**************************Fin de communication**************************");
		    }
	    }}
    }

    });
    
 //////////////////////////////////////


//////////////////////////////////////////
    	     //////////////// regetrer /////////////////////
    
    
    
    
    
    
    
    
    ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Extracteur" );
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
     
       public void Methode(String result){
         
        if  (result=="" )  
            System.out.print("==> "+result.length()+" agents   trouvés dans le DF."); }
       
           //bbGui1.showMsg("==> "+result.length()+" agents   trouvés dans le DF."); }
       
      
     protected void takeDown() {        
        //doDelete();          
                                             
        
      }
     
     
   
}