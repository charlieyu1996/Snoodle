package cloudcode.guestbook.frontend;

import java.io.InputStream;

public class SpeechModel {
        private InputStream inputStream;
        private String emailList;


        public InputStream getInputStream(){
            return inputStream;
        }

        public void setInputStream(InputStream inputStream){
            this.inputStream = inputStream;
        }

        public String getEmailList(){
            return emailList;
        }

        public void setEmailList(String emailList){
            this.emailList = emailList;
        }
}
