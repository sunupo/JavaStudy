



```java
/** 
 * 选择排序： 
 * 简单选择排序，树形选择排序与堆排序 
 *  
 */  
public class SelecSortDemo {  
      
    /** 
     * -------------------------------------------- 
     * 简单选择排序 
     * 原理：假设列表中有n个元素，从第一个元素开始，在第一个元素 
     * 与最后一个元素之间选择一个最小的元素与第一个元素交换， 
     * 然后从第二个元素开始，在第二个元素与最后一个元素之间选择 
     * 最小的元素与第二个元素交换，以此类推，最后列表有序。 
     */  
    public static void simpleSelectSort(Object[] a){  
        int len = a.length;  
        for(int i = 0,j;i<len;i++){  
            j = selectMin(a, i);  
            if(i!=j)    //等于就没有必要交换了  
                a[i] = a[j];  
        }  
    }  
    /** 
     * 简单选择排序的辅助方法 
     * 从指定位置i开始到最后位置选择出一个最小的元素 
     * 并且返回它的索引值 
     */  
    private static int selectMin(Object[] a,int low){  
        int min = low;  //假设第一个元素为最小值  
        for(int i = low+1;i<a.length;i++){  
            if(((Comparable)a[i]).compareTo(a[min])<0){  
                min = i;  
            }  
        }  
        return min;  
    }  
      
      
      
     /**  
     * ---------------------------------------  
     * 树形选择排序 ： 
     * 对于简单排序来说，主要是进行n-1趟元素的比较，每趟比较n-2次， 
     * 每趟比较取出一个最小值(也可以是最大值)，逐步使列表有序。 
     * 但是第一趟的比较是可以为后续的比较提供信息的，使后续的比较次数大大减少， 
     * 而后续的比较又可以为更后续的比较提供信息，这样就减少了比较的次数，减少了 
     * 时间复杂度。 
     *  
     * 实现原理： 
     * 第一步，首先对n个记录进行两两比较，得到较小的n/2个数再依次比较，依次类推 
     * 直到得到一个最小值,这是一个构造完全二叉树的过程，根节点即为最小元素，叶子节点为列表元素。 
     * 构造的此树的存储结构可以用数组表示方法，数组长度为2n-1。填充此树，比如 
     * 列表元素为：49    38     65    97   76    13    27   49 
     * 构造的树为：                     13 
     *                     38               13 
     *                38       65       13       27 
     *              19  38   65  97   76  13   27  49 
     * 13为根结点位最小值，列表元素为叶子节点 
     *  
     * 第二步，移走最小元素，此时可重新为数组a的第一个位置赋值为此最小值， 
     * 之后如果找出次小值则可以为第二个位置赋值，...... 
     *  
     * 第三步，找出次小值，找出最小值在叶子节点的位置，从该节点开始，和其兄弟节点 
     * 进行比较，修改从叶子节点到根节点的元素值，比较完毕后，根节点为次小值。 
     * 第三步比较是利用了第一次比较提供的信息，因为第一步已经得到了两两比较的 
     * 较小值，只要拿第一次与最小值比较的元素(即最小值的兄弟节点)与它们比较即可得最小值。 
     * 即拿上述例子的76与27比较，然后27与38比较得到次小值27。 
     * 重复第二和第三步，排序完成。 
     *  
     * PS:这里把移出去的叶子节点都要重设为最大值，可对此方法进行稍微改动 
     * 可传一个最大值进来，这里是整型所以用了Integer.MAX_VALUE 
     */    
    public static void treeSelectSort(Object[] a){    
       int len = a.length;  
       int treeSize = 2 * len - 1;  //完全二叉树的节点数  
       int low = 0;  
       Object[] tree = new Object[treeSize];    //临时的树存储空间  
       //由后向前填充此树，索引从0开始  
       for(int i = len-1,j=0 ;i >= 0; --i,j++){      //填充叶子节点  
           tree[treeSize-1-j] = a[i];  
       }  
         
       for(int i = treeSize-1;i>0;i-=2){ //填充非终端节点  
           tree[(i-1)/2] = ((Comparable)tree[i-1]).compareTo(tree[i]) < 0 ? tree[i-1]:tree[i];  
       }  
         
       //不断移走最小节点  
       int minIndex;  
       while(low < len){  
           Object min = tree[0];    //最小值  
           a[low++] = min;  
           minIndex = treeSize-1;         
           //找到最小值的索引  
           while(((Comparable)tree[minIndex]).compareTo(min)!=0){  
               minIndex--;  
           }  
           tree[minIndex] = Integer.MAX_VALUE;  //设置一个最大值标志  
           //找到其兄弟节点  
           while(minIndex > 0){      //如果其还有父节点  
               if(minIndex % 2 == 0){   //如果是右节点  
                   tree[(minIndex-1)/2] = ((Comparable)tree[minIndex-1]).compareTo(tree[minIndex])  
                        < 0 ? tree[minIndex-1]:tree[minIndex];  
                   minIndex = (minIndex-1)/2;  
               }else{                   //如果是左节点  
                    tree[minIndex/2] = ((Comparable)tree[minIndex]).compareTo(tree[minIndex+1])  
                        < 0 ? tree[minIndex]:tree[minIndex+1];  
                    minIndex = minIndex/2;  
               }  
           }  
             
       }  
    }  
  
  
      
        /** 
     * ---------------------------------- 
     * 堆排序 
     *    堆排序是在树形选择排序的基础上进一步进行优化 
     * 只需要一个额外的存储空间，且不需根据标志判断是不是最大值。 
     * 堆的定义：在1到n/2的元素中，有k(i)<=k(2i),k(i)<=k(2i+1) 
     * 或k(i)>=k(2i),k(i)>=k(2i+1) 
     * 简单来说：就是假如将此序列看成一棵完全二叉树，要使这个无序列表 
     * 变成堆，则小于等于n/2(最后一个非终端节点就是n/2)的某个节点i的左右子节点均大于此节点， 
     * 即堆的定义k(i)<=k(2i),k(i)<=k(2i+1)。 
     *  
     * 实现原理： 
     *    首先将序列看成一个树形结构， 
     * 1.构建堆的过程：找到最后一个非终端节点n/2，与它的左右子节点比较， 
     * 比较结果使此父节点为这三个节点的最小值。再找n/2-1这个节点， 
     * 与其左右子节点比较，得到最小值，以此类推....，最后根节点即为最小值 
     * 比如：49  38   65   97   76   13   27   49 
     * 初始树为： 
     *              49 
     *        38              65 
     *    97      76      13       27 
     * 49 
     * 构造堆后的树为 
     *              13 
     *       38              27 
     *    49    76       65       49 
     *  97 
     *  交换数据的顺序为：97<——>49, 13<--->65,38不用换，49<-->13,13<-->27 
     * 2.输出堆顶元素并调整建新堆的过程 
     *    输出堆顶最小值后，假设以最后一个值替代之，由于其左右子树的堆结构并没有被破坏 
     * 只需要自上而下进行调整。比如把上图的13输出后以97替代，然后可以把97与27交换， 
     * 然后97又与49交换，此时最小值为根元素27，输出27后以又用最后一个值替换根元素， 
     * 以此类推，则最终得到有序序列  
     */  
    public static void heapSort(Object[] a){    
        int len = a.length;    
        //构建堆    
        for(int i=(len-1)/2;i>=0;i--){    
            heapAdjust(a,i,len);    
        }    
            
        //输出堆顶元素并调整建新堆的过程    
        int count = len-1;    
        while(count > 0 ){    
            //交换树根与最后一个值    
            swap(a,0,count);    
            count -- ;    
            heapAdjust(a,0,count);    
        }    
    }    
        
    /**  
     * 调整某一个节点极其左右子节点的位置 ，并选择左右节点中的较大者 
     * 继续向下调整 
     */    
    private static void heapAdjust(Object[] a,int i,int len){    
        Object parent = a[i];  
        for(int j = (i+1) * 2 - 1;j < len; j = (j+1) * 2 - 1){   //沿着左右节点中的较小者继续往下搜索  
            if(j < len-1 && ((Comparable)a[j]).compareTo(a[j+1]) < 0 ){  
                ++j;        //如果左节点较大过度到右节点  
            }  
            if(((Comparable)parent).compareTo(a[j]) > 0) //左右节点均小于父节点则不必要继续向下搜索  
                break;    
            a[i] = a[j];  
            i = j ;  
        }  
        a[i] = parent;      //parent插入到正确的位置  
          
    }  
      
    /** 
     * 交换数组中两元素的值 
     */  
    private static void swap(Object[] a,int i,int j){  
        Object temp = null;  
        temp = a[i];  
        a[i] = a[j];  
        a[j] = temp;  
    }  
      
    //just for test  
    public static void main(String[] args) {  
        Integer[] data = {49,38,65,97,76,13,27,49};  
        SelecSortDemo.treeSelectSort(data);  
        for(Integer d:data){  
            System.out.println(d);  
        }  
    }  
}
```

