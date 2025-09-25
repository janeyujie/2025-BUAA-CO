#include<stdio.h>

void array_func(int arr[]) {
	printf("arr[0] = %d\n", arr[0]);
	arr[0] = 999;
	printf("new arr[0] = %d\n", arr[0]);
	return;
}
void static_counter(){
	static int cnt = 0;
	static int i1 = 0,i2 = 0;
	cnt = cnt + 1;
	printf("static counter = %d\n",cnt);
}
int main(){
	printf("22241014\n");
	int a[5] = {1, 2, 3, 4, 5};
	printf("a[0] = %d\n", a[0]);
	array_func(a);
	printf("after func a[0] = %d\n", a[0]);
	
	static_counter();
	static_counter();
	static_counter();
	
	printf("a[4] = %d\n",a[4]);
	printf(" !()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~\n");
	printf("B-test2 ended\n");
	return 0;
}
