@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY", "MAGIC_NUMBER")

package org.jetbrains.research.code.submissions.clustering.load.distance

data class GumTreeDistanceTestCodeSnippets(
    val code1: String,
    val code2: String,
    val distance1: Int,
    val distance2: Int,
)

object GumTreeDistanceCodeSnippetsData {
    private val changes1 = GumTreeDistanceTestCodeSnippets(
        """
import sys


def f1(f1_p1, f1_p2):
    print(abs((f1_p1 % f1_p2) - pow(f1_p2, f1_p1)))


f1(int(input()), int(input()))
sys.exit()
                    """.trimIndent(),
        """
def f1(f1_p1, f1_p2):
    print(abs(((f1_p1 % f1_p2) - pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        59,
        59,
    )
    private val changes2 = GumTreeDistanceTestCodeSnippets(
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
        40,
        40,
    )
    private val changes3 = GumTreeDistanceTestCodeSnippets(
        """
def f1(f1_p1, f1_p2):
    print(abs(((f1_p1 % f1_p2) - pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        """
def f1(f1_p1, f1_p2):
    print(abs(pow(f1_p2, f1_p1) - (f1_p1 // f1_p2) - f1_p1))
                    """.trimIndent(),
        33,
        33,
    )
    private val changes4 = GumTreeDistanceTestCodeSnippets(
        """
def f1(f1_p1, f1_p2): 
    print(abs((f1_p1 % f1_p2) - (pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        """
import math


def f1(f1_p1, f1_p2): 
    print(int(math.fabs(f1_p1 % f1_p2 - math.pow(f1_p2, f1_p1))))
                    """.trimIndent(),
        57,
        57,
    )
    private val changes5 = GumTreeDistanceTestCodeSnippets(
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
        288,
        281,
    )
    val codeSnippets = listOf(
        changes1,
        changes2,
        changes3,
        changes4,
        changes5,
    )
}
