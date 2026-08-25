import java.util.Arrays;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bubble");
        int[] bubbleArr = {1, 2, 5, 3, 4, 7, 6, 5};
        System.out.println("Before : " + Arrays.toString(bubbleArr));
        bubbleSort(bubbleArr);
        System.out.println("After  : " + Arrays.toString(bubbleArr));
        System.out.println("Merge");
        int[] mergeArr = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("Before : " + Arrays.toString(mergeArr));
        mergeSort(mergeArr, 0, mergeArr.length - 1);
        System.out.println("After  : " + Arrays.toString(mergeArr));
        System.out.println("Brackets");
        String test1 = "[[()]]";
        String test2 = "[][[]";
        System.out.println(test1 + " -> " + isValidBrackets(test1)); // True
        System.out.println(test2 + " -> " + isValidBrackets(test2)); // False
    }
    public static void bubbleSort(int[] arr) {
        int len = arr.length;
        for (int i = 0; i < len - 1; i++) {
            boolean isSorted = true;
            for (int j = 0; j < len - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    isSorted = false;
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
            if (isSorted) {
                break;
            }
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

    public static boolean isValidBrackets(String s) {
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