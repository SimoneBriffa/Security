package com.supportportal.Security.configuration;

public class Sort {

    public static int[] selectionSort(int[] list){

        int i, j, minValue, minIndex, temp=0;

        for(i=0; i<list.length; i++){
            minValue=list[i];
            minIndex=i;
            for(j=i; j<list.length; j++){
                if(list[j]<minValue){
                    minValue=list[j];
                    minIndex=j;
                }
            }
            if(minValue<list[i]){
                temp=list[i];
                list[i]=list[minIndex];
                list[minIndex]=temp;
            }
        }
        return list;
    }


    public static int[] bubbleSort(int[] list){

        int i, j, temp = 0;

        for(i=0; i<list.length-1; i++){
            for(j=0; j<list.length-1-i; j++){
                if(list[j] > list[j+1]){
                    temp=list[j];
                    list[j]=list[j+1];
                    list[j+1]=temp;
                }
            }
        }
        return list;
    }

    public static void main(String[] args) {

        int i = 10;
        int j = 20;

        int x = ++j;

        System.out.println(x);
        System.out.println(x);

    }
}
