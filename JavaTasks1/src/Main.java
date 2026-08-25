import java.util.Arrays;
import java.util.Stack;

class Main {
    public static void main() {
        int[] arr1 = {1,2,5,3,4,7,6,5};
        System.out.println("Array before Bubble Sort: " + Arrays.toString(arr1));
        Bubble(arr1);
        System.out.println("Array after Bubble Sort: " + Arrays.toString(arr1));
        int[] arr2 = {1,2,5,3,4,7,6,5};
        System.out.println("Array Before Merge Sort: " + Arrays.toString(arr2));
        mergeSort(arr2, 0, arr2.length - 1);
        System.out.println("Array After Merge Sort:  " + Arrays.toString(arr2));
        String test1 = "[[(hahhaha)]]";
        String test2 = "[][a[k]";
        System.out.println(test1 + ": " + isValid(test1));
        System.out.println(test2 + ": " + isValid(test2));
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
            if(a) break;
        }
    }

    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    public static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];
        for (int i = 0; i < n1; i++) leftArr[i] = arr[left + i];
        for (int j = 0; j < n2; j++) rightArr[j] = arr[mid + 1 + j];
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArr[i] <= rightArr[j]) {
                arr[k++] = leftArr[i++];
            } else {
                arr[k++] = rightArr[j++];
            }
        }
        while (i < n1) arr[k++] = leftArr[i++];
        while (j < n2) arr[k++] = rightArr[j++];
    }

    public static boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '(':
                case '[':
                case '{':
                    stack.push(ch);
                    break;
                case ')':
                    if (stack.isEmpty() || stack.pop() != '(') return false;
                    break;
                case ']':
                    if (stack.isEmpty() || stack.pop() != '[') return false;
                    break;
                case '}':
                    if (stack.isEmpty() || stack.pop() != '{') return false;
                    break;
                default:
                    break;
            }
        }
        return stack.isEmpty();
    }
}