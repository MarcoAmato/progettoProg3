package server;

import java.io.IOException;
import java.io.ObjectInputStream;

class Common {
    public static <E> E getInputOfClass(ObjectInputStream inputStream, Class expectedInputClass){
        E sanitizedInput = null;

        try{
            Object input = inputStream.readObject();
            if(input == null){
                throw new Error("Expected "+ expectedInputClass.getName() +", received null");
            }else if (!(input.getClass() == expectedInputClass)){
                throw new Error("Expected "+ expectedInputClass.getName() +", received object of class: " + input.getClass());
            }
            else{
                //input is not null and type arraylist
                sanitizedInput = (E)input;
            }
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

        return sanitizedInput;
    }
}
