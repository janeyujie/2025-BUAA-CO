#include<stdio.h>
int var = 0;
int check(){
	var = var + 1;
	printf("calling func check\n");
	return 1;
}
int main(){
	printf("22241014\n");
	printf("now var = %d\n",var);
	if (0 && check() == 1) {
	}
	printf("after && var = %d\n",var);
	
	printf("now var = %d\n", var);
	if(1 || check() == 1){
	}
	printf("after || var = %d\n",var);
	
	printf("now var = %d\n", var);
	if(1 && check() == 1){
	}
	printf("after && var = %d\n",var);
	
	if(0 || check() == 1){
	}
	printf("after || var = %d\n",var);
	return 0;
}
