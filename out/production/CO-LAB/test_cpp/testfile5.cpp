#include<stdio.h>
const int c = 1;

int int_func(int p) {
	printf("input param %d\n",p);
	return p*p;
}
int main(){
	printf("22241014\n");
	int i = 0;
	int sum = 0;
	int res = int_func(5);
	
	printf("int_func(%d) is %d\n",5,res);
	if (res==25) {
		printf("res is 25\n");
	} else {
		printf("res is not 25\n");
	}
	
	if (res!=25) {
		printf("res is or not 25?\n");
	}
	
	for (i = 0; i < 6; i = i + 1) {
		if (i == 2){
			printf("continue at 2\n");
			continue;
		}
		
		if (i > 4) {
			printf("break at 5\n");
			break;
		}
		sum = sum + i;
		
	}
	int pos_sum = +sum;
	int neg_sum = -sum;
	printf("sum = %d\n",sum);
	for (; i<10;) {
		i = i + (1);
		break;
	}
	for (i=20;;) {
		printf("i = %d\n",i);
		break;
		
	}
	for(;;i=i+1)
	{
		printf("i = %d\n",i);
		break;
	}
	printf("C-test2 ended\n");
	return 0;
}
