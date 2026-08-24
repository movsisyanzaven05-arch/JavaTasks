class Main {
    public static void main() {
        int[] arr = {1,2,5,3,4,7,6,5};
        System.out.print("Array before : ");
        for(int a: arr)
        {
            System.out.print(a + " ");
        }
        System.out.println();
        Bubble(arr);
        System.out.print("Array after  : ");
        for(int a: arr)
        {
            System.out.print(a + " ");
        }
    }

    public static void Bubble(int[] arr)
    {
        int len = arr.length;
        for (int i = 0; i < len - 1; i += 1)
        {
            boolean a = true;
            for(int j = i; j < len - 1; j += 1)
            {
                if (arr[j] > arr[j + 1])
                {
                    a = false;
                    int t = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = t;
                }
            }
            if(a)
            {
                break;
            }
        }
    }
}
