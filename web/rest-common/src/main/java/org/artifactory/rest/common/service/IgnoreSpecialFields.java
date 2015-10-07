package org.artifactory.rest.common.service;

import java.lang.annotation.*;

/**
 * @author Chen Keinan
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IgnoreSpecialFields {
    /**
     *  list of fields define in this method will be ignored during json serialization
     *  the class that have this annotation need to :
     *  1.  override toString by calling this function: JsonUtil.jsonToStringIgnoreSpecialFields(this);
     *  2.  set @JsonFilter("exclude fields") annotation for this class
     *
     *  Note: for conditional ignore special fields
     *  must implements ISpecialFields interface
     * @return list of fields to be ignored on return
     */
    String[] value() default {};
}
