#include<stdio.h>
int main() {
	printf("22241014\n");
	const int l = 5;
	int a = 1;
	int b;
	int c1 = 10, c2 = 20;
	b = a + l;
	
	printf("a = %d\n", a);
	printf("int b = %d\n", b);
	
	int v2 = c1 * c2;
	printf("v2 = %d\n", v2);
	printf("b = %d\n", b);
	
	if (a < b) {
		{
			int a = 100;
			printf("a = %d\n", a);
		}
	} 
	
	printf("a = %d\n",a);
	printf("I'm a void sentence\n");
	;
	printf("c1 = %d\n",c1);
	printf("I'm a void sentence\n");
	
	return 0;
} 
