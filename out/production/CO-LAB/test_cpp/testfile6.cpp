#include<stdio.h>
const int c1=1,c2=2;
int add_and_mul(int p1, int p2, int p3) {
	printf("culculate add and mul\n");
	return p1 - p2 * p3;
}
int main(){
	printf("22241014\n");
	int a = 1023456789, b = 3;
	int res1 = add_and_mul(a, b, 2);
	
	printf("add_and_mul res = %d\n",res1);
	int mod_res = a % b;
	printf("mod_res = %d\n", mod_res);
	int div_res = a/b;
	printf("division res = %d\n", div_res);
	
	int i,j;
	for(i = 0, j = 2; ; i=i+1) {
		printf("loooooop\n");
		if (!i >= 1) break;
	}
	
	for(;;){
		printf("loooooopppppp\n");
		break;
	}
	
	if(mod_res <= div_res) {
		printf("mod <= div\n");
	}
	
	i = 0;
	for( ; i < 1; i=i+1){
		printf("lllllloop\n");
	}
	for(i = 1;i < 1;){
	}
	
	{
	}
	
	printf("C-test3 ended\n");
	return 0; 
}
