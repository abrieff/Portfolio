import java.util.Collections;

int countBoolean(boolean[] b) {
  int c = 0;
  
  for( int i=0; i<b.length; i++ ) {
    if(b[i]) {
      c++;
    }
  }
  
  return c;
}

String[] concatStringArrays(String[] s1, String[] s2) {
  ArrayList<String> s = new ArrayList<String>();
  
  Collections.addAll(s,s1);
  Collections.addAll(s,s2);
  
  String[] sout = new String[s.size()];
  sout = s.toArray(sout);
  
  return sout;
}

String[] unique(String[] sin) {
  ArrayList<String> s = new ArrayList<String>();
  
  s.add(sin[0]);
  
  for( int i=1; i<sin.length; i++ ) {
    if( !s.contains(sin[i]) ) {
      s.add(sin[i]);
    }
  }
  
  String[] sout = new String[s.size()];
  sout = s.toArray(sout);
  
  return sout;
}

boolean contains(int x[], int e) {
  
  for( int i=0; i<x.length; i++ ) {
    if( x[i] == e ) {
      return true;
    }
  }
  
  return false;
}

float min_arraylist(ArrayList<Float> x) {
  
  if( x.size()==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float minx = x.get(0);
  
  for (int i = 1; i < x.size(); i++) {
      if (x.get(i) < minx) {
        minx = x.get(i);
      }
  }
  
  return minx;
}

float max_arraylist(ArrayList<Float> x) {
  
  if( x.size()==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float maxx = x.get(0);
  
  for (int i = 1; i < x.size(); i++) {
      if (x.get(i) > maxx) {
        maxx = x.get(i);
      }
  }
  
  return maxx;
}

float min_array(float[] x, int len) {
  
  if( len==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float minx = x[0];
  
  for (int i = 1; i < len; i++) {
      if (x[i] < minx) {
        minx = x[i];
      }
  }
  
  return minx;
}

float max_array(float[] x, int len) {
  
  if( len==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float maxx = x[0];
  
  for (int i = 1; i < len; i++) {
      if (x[i] > maxx) {
        maxx = x[i];
      }
  }
  
  return maxx;
}

float sum_array(float[] x) {
  float s = 0;
  
  for( int i=0; i<x.length; i++ ) {
    s = s+x[i];
  }
  
  return s;
}

int sum_array(int[] x) {
  int s = 0;
  
  for( int i=0; i<x.length; i++ ) {
    s = s+x[i];
  }
  
  return s;
}
