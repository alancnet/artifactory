package org.artifactory.security;

/**
 * Defines the plugin realm authentication policy
 *
 * @author Shay Yaakov
 * @since 4.1.0
 */
public enum RealmPolicy {

    /**
     * Executed in the start of the authentication chain before any other built-in authentication realms (Ldap, Internal etc.)
     * When authentication succeeds, all other built-in realms are skipped. This is the default behavior
     */
    SUFFICIENT,

    /**
     * Executed additionally if at least one of the previous authentication provider has succeeded
     */
    ADDITIVE
}