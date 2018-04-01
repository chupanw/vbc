// Generated from JavadocParser.g4 by ANTLR 4.3

package edu.cmu.cs.vbc.prog.checkstyle.grammars.javadoc;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JavadocParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JavadocParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JavadocParser#dd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDd(@NotNull JavadocParser.DdContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#isindexTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsindexTag(@NotNull JavadocParser.IsindexTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlComment(@NotNull JavadocParser.HtmlCommentContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tbody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTbody(@NotNull JavadocParser.TbodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#imgTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImgTag(@NotNull JavadocParser.ImgTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#trTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrTagOpen(@NotNull JavadocParser.TrTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(@NotNull JavadocParser.BodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#dt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt(@NotNull JavadocParser.DtContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#basefrontTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasefrontTag(@NotNull JavadocParser.BasefrontTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference(@NotNull JavadocParser.ReferenceContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#pTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPTagOpen(@NotNull JavadocParser.PTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#trTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrTagClose(@NotNull JavadocParser.TrTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#html}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtml(@NotNull JavadocParser.HtmlContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(@NotNull JavadocParser.TextContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#frameTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameTag(@NotNull JavadocParser.FrameTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#paramTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamTag(@NotNull JavadocParser.ParamTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#colgroupTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColgroupTagOpen(@NotNull JavadocParser.ColgroupTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#baseTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseTag(@NotNull JavadocParser.BaseTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#metaTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetaTag(@NotNull JavadocParser.MetaTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#javadocInlineTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJavadocInlineTag(@NotNull JavadocParser.JavadocInlineTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#javadocTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJavadocTag(@NotNull JavadocParser.JavadocTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#headTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeadTagOpen(@NotNull JavadocParser.HeadTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tdTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTdTagOpen(@NotNull JavadocParser.TdTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#ddTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdTagClose(@NotNull JavadocParser.DdTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#thTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThTagOpen(@NotNull JavadocParser.ThTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#brTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBrTag(@NotNull JavadocParser.BrTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#theadTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTheadTagOpen(@NotNull JavadocParser.TheadTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#bodyTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBodyTagClose(@NotNull JavadocParser.BodyTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#pTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPTagClose(@NotNull JavadocParser.PTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlElement(@NotNull JavadocParser.HtmlElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#parameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameters(@NotNull JavadocParser.ParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption(@NotNull JavadocParser.OptionContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#paragraph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParagraph(@NotNull JavadocParser.ParagraphContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlTag(@NotNull JavadocParser.HtmlTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#headTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeadTagClose(@NotNull JavadocParser.HeadTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#colTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColTag(@NotNull JavadocParser.ColTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#thead}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThead(@NotNull JavadocParser.TheadContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#singletonTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingletonTag(@NotNull JavadocParser.SingletonTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#liTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiTagClose(@NotNull JavadocParser.LiTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tdTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTdTagClose(@NotNull JavadocParser.TdTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#description}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescription(@NotNull JavadocParser.DescriptionContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlElementOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlElementOpen(@NotNull JavadocParser.HtmlElementOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#thTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThTagClose(@NotNull JavadocParser.ThTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#areaTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAreaTag(@NotNull JavadocParser.AreaTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#linkTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinkTag(@NotNull JavadocParser.LinkTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tfootTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTfootTagOpen(@NotNull JavadocParser.TfootTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#head}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHead(@NotNull JavadocParser.HeadContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#javadoc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJavadoc(@NotNull JavadocParser.JavadocContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlElementClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlElementClose(@NotNull JavadocParser.HtmlElementCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#optionTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionTagOpen(@NotNull JavadocParser.OptionTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#dtTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDtTagClose(@NotNull JavadocParser.DtTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(@NotNull JavadocParser.AttributeContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#liTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiTagOpen(@NotNull JavadocParser.LiTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#wrongSinletonTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrongSinletonTag(@NotNull JavadocParser.WrongSinletonTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#optionTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionTagClose(@NotNull JavadocParser.OptionTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#hrTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHrTag(@NotNull JavadocParser.HrTagContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tbodyTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTbodyTagOpen(@NotNull JavadocParser.TbodyTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tbodyTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTbodyTagClose(@NotNull JavadocParser.TbodyTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#colgroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColgroup(@NotNull JavadocParser.ColgroupContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#theadTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTheadTagClose(@NotNull JavadocParser.TheadTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlTagOpen(@NotNull JavadocParser.HtmlTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tfootTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTfootTagClose(@NotNull JavadocParser.TfootTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#td}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTd(@NotNull JavadocParser.TdContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tfoot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTfoot(@NotNull JavadocParser.TfootContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#bodyTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBodyTagOpen(@NotNull JavadocParser.BodyTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#dtTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDtTagOpen(@NotNull JavadocParser.DtTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#th}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTh(@NotNull JavadocParser.ThContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#singletonTagName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingletonTagName(@NotNull JavadocParser.SingletonTagNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#ddTagOpen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdTagOpen(@NotNull JavadocParser.DdTagOpenContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#singletonElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingletonElement(@NotNull JavadocParser.SingletonElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#htmlTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlTagClose(@NotNull JavadocParser.HtmlTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#li}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLi(@NotNull JavadocParser.LiContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#colgroupTagClose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColgroupTagClose(@NotNull JavadocParser.ColgroupTagCloseContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#tr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTr(@NotNull JavadocParser.TrContext ctx);

	/**
	 * Visit a parse tree produced by {@link JavadocParser#inputTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputTag(@NotNull JavadocParser.InputTagContext ctx);
}