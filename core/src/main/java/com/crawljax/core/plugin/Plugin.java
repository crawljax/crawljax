package com.crawljax.core.plugin;

/**
 * Main interface for all type of plugins, there are 8 different types of Plugnis.
 * <table>
 * <tbody>
 * <tr>
 * <th>Type</th>
 * <th>Executed</th>
 * <th>Examples</th>
 * </tr>
 * <tr>
 * <td>OnNewStatePlugin</td>
 * <td>When a new state is found while crawling</td>
 * <td>Create Screenshots, Validate DOM</td>
 * </tr>
 * <tr>
 * <td>OnRevisitStatePlugin</td>
 * <td>When a state is revisited</td>
 * <td>Crawljax benchmarking</td>
 * </tr>
 * <tr>
 * <td>OnUrlLoadPlugin</td>
 * <td>After the initial URL is (re)loaded</td>
 * <td>Reset back-end state</td>
 * </tr>
 * <tr>
 * <td>OnInvariantViolationPlugin</td>
 * <td>When an invariant fails validation</td>
 * <td>Report builder</td>
 * </tr>
 * <tr>
 * <td>PreStateCrawlingPlugin</td>
 * <td>Before a new state is crawled</td>
 * <td>Logging candidate elements</td>
 * </tr>
 * <tr>
 * <td>PostCrawlingPlugin</td>
 * <td>After the crawling</td>
 * <td>Generating tests from the state machine</td>
 * </tr>
 * <tr>
 * <td>ProxyServerPlugin</td>
 * <td>Before the crawling, at the initialization of the core</td>
 * <td>Loading a custom proxy configuration in the used browser</td>
 * </tr>
 * </tbody>
 * </table>
 */
public interface Plugin {

}
