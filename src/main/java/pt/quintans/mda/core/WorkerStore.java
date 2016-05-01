package pt.quintans.mda.core;

public class WorkerStore {

     private static final ThreadLocal < Work > store = 
         new ThreadLocal < Work > () {
             @Override protected Work initialValue() {
                 return new Work();
             }
     	};
 
     public static Work get() {
         return store.get();
     }

}
