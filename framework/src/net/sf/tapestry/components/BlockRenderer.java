package net.sf.tapestry.components;

import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRender;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;

/**
 * An implementation of IRender that renders a Block component.
 * 
 * <p>The BlockRenderer allows the contents of a {@link Block} to be rendered
 * via {@link IRender}. It can be used in cases when an {@link IRender} object is
 * required as an argument or a binding to render a part of a Component. 
 * To provide a complicated view, it could be defined in a {@link Block} and then
 * returned encapsulated in a BlockRenderer.
 * 
 * <p>It is important to note that a special care has to be taken if 
 * the BlockRenderer is used within an inner class of a component or a page. 
 * In such a case the instance of the component that created the inner class 
 * may not be the currently active instance in the RequestCycle when the 
 * BlockRenderer is required. Thus, calling getComponent("blockName") to get the
 * block component may return a Block component that is not initialized for this 
 * RequestCycle.
 * 
 * <p>To avoid similar problems, the ComponentAddress class could be used in
 * conjunction with BlockRenderer. 
 * Here is a quick example of how BlockRenderer could be used with ComponentAddress:
 * <p>
 * <code>
 * <br>// Create a component address for the current component
 * <br>final ComponentAddress address = new ComponentAddress(this);
 * <br>return new SomeClass() {
 * <br>&nbsp;&nbsp;IRender getRenderer(IRequestCycle cycle) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;MyComponent component = (MyComponent) address.findComponent(cycle);
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// initialize variables in the component that will be used by the block here
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;return new BlockRenderer(component.getComponent("block"));
 * <br>&nbsp;&nbsp;}
 * <br>}
 * </code>
 * 
 * @version $Id$
 * @author mindbridge
 * @since 2.2
 */
public class BlockRenderer implements IRender
{
	private Block m_objBlock;

	/**
	 * Creates a new BlockRenderer that will render the content of the argument
	 * @param objBlock the Block to be rendered
	 */
	public BlockRenderer(Block objBlock)
	{
		m_objBlock = objBlock;
	}

	/**
	 * @see net.sf.tapestry.IRender#render(IMarkupWriter, IRequestCycle)
	 */
	public void render(IMarkupWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		m_objBlock.renderBody(writer, cycle);
	}

}
