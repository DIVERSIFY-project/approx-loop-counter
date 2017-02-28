
public class TestClass {

    public void WithApproxLoop(int[] a) {
        for ( int i = 0; i < a.length; i++ ) {
            a[i] = a[i] * 2;
        }
    }

    public int WithoutInnerMostApproxLoop(int[] a) {
        int k = 0;
        for ( int i = 0; i < a.length; i++ ) {
            a[i] = a[i] * 2;
            for ( int j = 0; j < a.length; j++ ) {
                k += a[j];
            }
        }
        return k;
    }

    public int MoreThanOneArrayAccess(int[] a, int[] b) {
        int k = 0;
        for ( int i = 0; i < a.length; i++ ) {
            a[i] = a[i] * 2;
            b[i] = a[i] * 3;
        }
        return k;
    }

    public int NonApproximable(int[] a, int[] b) {
        int k = 0;
        for ( int i = 0; i < a.length; i++ ) {
            k += a[i];
        }
        return k;
    }

    public void WithInnerBlock(int[] a, int[] b) {
        int k = 0;
        for ( int i = 0; i < a.length; i++ ) {
            a[i] = a[i] * 2;
            if ( a[i] > 10 ) {
                a[i] = a[i] - 1;
            }
        }
    }

    public void WithInnerBlockSwitch(int[] a, int b) {
        int k = 0;
        for ( int i = 0; i < a.length; i++ ) {
            a[i] = a[i] * 2;
            switch (b) {
                case 1: a[i] = a[1]; break;
                case 2: a[i] = a[2]; break;
            }
        }
    }

}