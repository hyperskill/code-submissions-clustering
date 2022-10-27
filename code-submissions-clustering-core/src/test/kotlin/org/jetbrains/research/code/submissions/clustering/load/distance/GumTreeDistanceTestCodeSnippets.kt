@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY", "MAGIC_NUMBER")

package org.jetbrains.research.code.submissions.clustering.load.distance

data class GumTreeDistanceTestCodeSnippets(
    val code1: String,
    val code2: String,
    val distance: Int
)

object GumTreeDistanceCodeSnippetsData {
    private val smallChanges = GumTreeDistanceTestCodeSnippets(
        """
import sys


def f1(f1_p1, f1_p2):
    print(abs((f1_p1 % f1_p2) - pow(f1_p2, f1_p1)))


f1(int(input()), int(input()))
sys.exit()
                    """.trimIndent(), """
def f1(f1_p1, f1_p2):
    print(abs(((f1_p1 % f1_p2) - pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        5
    )
    private val smallChanges1 = GumTreeDistanceTestCodeSnippets(
        """
a = input()
b = input()
c = int(a) + int(b) - 100
print(c)
        """.trimIndent(),
        """
a = int(input())
b = int(input())
c = a + b - 100
print(c)            
        """.trimIndent(),
        5
    )
    private val middleChanges1 = GumTreeDistanceTestCodeSnippets(
        """
def f1(f1_p1, f1_p2):
    print(abs(((f1_p1 % f1_p2) - pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        """
def f1(f1_p1, f1_p2):
    print(abs(pow(f1_p2, f1_p1) - (f1_p1 // f1_p2) - f1_p1))
                    """.trimIndent(),
        12
    )
    private val middleChanges2 = GumTreeDistanceTestCodeSnippets(
        """
def f1(f1_p1, f1_p2): 
    print(abs((f1_p1 % f1_p2) - (pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        """
import math


def f1(f1_p1, f1_p2): 
    print(int(math.fabs(f1_p1 % f1_p2 - math.pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        13
    )
    private val bigChanges = GumTreeDistanceTestCodeSnippets(
        """
v1 = [float(v2) for v2 in input().split()]
v3 = []
for v4 in v1:
    v3.append(v4 * 1000)
v5 = []
for v4 in sorted(v3, reverse=True):
    v5.append(v4 / 1000)
print(v5)
""".trimIndent(),
        """
print([float(v1) for v1 in sorted(input().split(), reverse=True)])
        """.trimIndent(),
        31
    )

    val codeSnippets = listOf(
        smallChanges,
//        smallChanges1,
//        middleChanges1,
//        middleChanges2,
//        bigChanges
    )
}
