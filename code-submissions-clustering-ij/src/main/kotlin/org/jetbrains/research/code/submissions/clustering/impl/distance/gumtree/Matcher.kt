package org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree

import com.github.gumtreediff.actions.EditScriptGenerator
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.matchers.MappingStore
import com.github.gumtreediff.matchers.Matcher
import com.github.gumtreediff.matchers.optimal.rted.RtedMatcher
import com.github.gumtreediff.tree.TreeContext

class Matcher(
    private val srcContext: TreeContext,
    private val dstContext: TreeContext
) {
    fun getEditActions(): List<Action> {
        val defaultMatcher: Matcher = RtedMatcher()
        val mappings: MappingStore = defaultMatcher.match(srcContext.root, dstContext.root)
        val editScriptGenerator: EditScriptGenerator = SimplifiedChawatheScriptGenerator()
        return editScriptGenerator.computeActions(mappings).asList()
    }
}
