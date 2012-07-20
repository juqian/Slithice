package jqian.util.eclipse;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

//import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionResolver {
    public static Object getAdapter(Object adaptable, Class<?> c) {
        if (c.isInstance(adaptable)) {
            return adaptable;
        }
        if (adaptable instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) adaptable;
            Object adapter = a.getAdapter(c);
            if (c.isInstance(adapter)) {
                return adapter;
            }
        }
        return null;
    }

    private static Object[] getSelectedResources(IStructuredSelection selection,Class<?> c) {
        return getSelectedAdaptables(selection, c);
    }

    private static Object[] getSelectedAdaptables(ISelection selection, Class<?> c) {
        ArrayList<Object> result = null;
        if (!selection.isEmpty()) {
            result = new ArrayList<Object>();
            Iterator<?> elements = ((IStructuredSelection) selection).iterator();
            while (elements.hasNext()) {
                Object adapter = getAdapter(elements.next(), c);
                if (c.isInstance(adapter)) {
                    result.add(adapter);
                }
            }
        }
        if (result != null && !result.isEmpty()) {
            return result.toArray((Object[]) Array
                    .newInstance(c, result.size()));
        }
        return (Object[]) Array.newInstance(c, 0);
    }
    
    public static IResource getSelectedResource(ISelection selection,int kind){
        IResource[] rs = (IResource[]) getSelectedResources(
                         (IStructuredSelection) selection, IResource.class);
        IResource result = null;
        for (int i = 0; i < rs.length; i++) {
            IResource r = rs[i];
            if (r.getType() == kind) {
                result = r;
                break;
            }
        }
        return result;
    }
   
}
