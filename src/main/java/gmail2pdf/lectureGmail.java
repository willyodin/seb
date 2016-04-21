/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmail2pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.BodyPart;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;

public class lectureGmail {

    private static final Logger LOG = Logger.getLogger(lectureGmail.class.getName());
    private final String dossierpdf="C:\\Users\\seb\\Documents\\pdf";
    private final Document document = new Document();
 public static void main(String[] args) {
     
  lectureGmail gmail = new lectureGmail();
  
  gmail.read("test.pdf");
 }

 public void read(String nom) {
  Properties props = new Properties();
  try (OutputStream file = new FileOutputStream(new File(dossierpdf+"\\"
            +nom))){
      PdfWriter writer = PdfWriter.getInstance(document, file);
            document.open();
   //props.load(new FileInputStream(new File("C:\\smtp.properties")));
   Session session = Session.getDefaultInstance(props, null);

   Store store = session.getStore("imaps");
   store.connect("smtp.gmail.com", "adressemail","motdepasse");

   Folder inbox = store.getFolder("inbox");
   inbox.open(Folder.READ_ONLY);
   int messageCount = inbox.getMessageCount();

   LOG.log(Level.INFO,"Lecture ");

   Message[] messages = inbox.getMessages();
   System.out.println("------------------------------");
   for (Message message:messages) {
       nouveauMail(message);
     LOG.log(Level.INFO,"sujet : {0}",message.getSubject());
     if (message.isMimeType("text/*")){
          StringBuffer sb = new StringBuffer((String)message.getContent());
      
   LOG.log(Level.INFO,"ajout texte : {0}",sb);
            ajoutCorps(sb);   
     }else{
     Multipart mp = (Multipart) message.getContent();
     for(int i=0;i<mp.getCount();i++) {
         
         LOG.log(Level.INFO,"partie {0}",i);
    BodyPart bodyPart = mp.getBodyPart(i);
         LOG.log(Level.INFO,"type : {0}",bodyPart.getContentType());
    if (bodyPart.isMimeType("text/*")) {
        StringBuffer sb = new StringBuffer((String)bodyPart.getContent());
        if (partisHTML(sb)){
            LOG.log(Level.INFO,"HTML ");
            //creationPDF(sb);
        }else{
        LOG.log(Level.INFO,"ajout texte : {0}",sb);
            ajoutCorps(sb);
                }
    }
    if (bodyPart.isMimeType("IMAGE/JPEG")){
        ajoutImage(bodyPart.getInputStream());
    }
}
     }
     System.out.println("------------------------------");
   }
   inbox.close(true);
   store.close();
document.close();
  } catch (Exception ex) {
    LOG.log(Level.SEVERE, null, ex);}
 }      
public boolean partisHTML(StringBuffer sb){
    boolean HTML =false;
    String s=sb.toString();
    if (s.startsWith("<!DOCTYPE html>")||s.startsWith("<html")){
        HTML=true;
    }
    return HTML;
}
public void ajoutCorps(StringBuffer sb){
        try {
            Paragraph p =new Paragraph(sb.toString());
            document.add(p);
        } catch (DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
   
        
}

public void ajoutExpediteur(Address sb){
        try {
            Paragraph p =new Paragraph(sb.toString());
            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLDITALIC);
            p.setFont(font);
            document.add(p);
        } catch (DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
   
        
}
public void ajoutSujet(String sujet){
        try {
            Paragraph p =new Paragraph(sujet);
            p.setAlignment(Element.ALIGN_CENTER);
            Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            p.setFont(font);
            document.add(p);
        } catch (DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
   
        
}
public void ajoutImage(InputStream imgStream){
        try {
               
           Image image;
           BufferedImage tampon = ImageIO.read(imgStream);
           int widthInitial = tampon.getWidth();
           int heightInitial=tampon.getHeight();
            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
               - document.rightMargin()) / widthInitial) ;
            
            image = Image.getInstance(            tampon.getScaledInstance(Math.round(widthInitial*scaler), Math.round(heightInitial*scaler), 0),null);
//image.scalePercent(scaler);

            document.add(image);
        } catch (IOException|DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } 
   
        
}

public void ajoutDate(Date quand){
        try {
            Paragraph p =new Paragraph(new SimpleDateFormat("dd MMMM yyyy").format(quand));
            Font font = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);
            p.setFont(font);
            document.add(p);
        } catch (DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
   
        
}
public void nouveauMail(Message message){
        try {
            Paragraph p =new Paragraph(" ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");
            document.add(p);
            ajoutDate(message.getSentDate());
            for (Address expediteur:message.getFrom()){
     LOG.log(Level.INFO,"expediteur : {0}",expediteur );
         ajoutExpediteur(expediteur);
     }
            ajoutSujet(message.getSubject());
        } catch (MessagingException|DocumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }  
    
}

}