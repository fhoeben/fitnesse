package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.responders.ChunkingResponder;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testrunner.run.PagePosition;
import fitnesse.testrunner.run.PagePositions;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Responder to display the tests that would be executed, in which order, for a given suite.
 */
public class PartitionPreviewResponder extends ChunkingResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    Response response = super.makeResponse(context, request);
    response.setContentType("text/tab-separated-values");
    return response;
  }

  @Override
  protected void doSending() throws Exception {
    PagePositions pages = getPagesToRun();
    try {
      Writer writer = response.getWriter();
      pages.appendTo(writer, "\t");
    } finally {
      response.close();
    }
  }

  protected PagePositions getPagesToRun() {
    SuiteFilter filter = SuiteResponder.createSuiteFilter(request, page.getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    List<WikiPage> allPages = suiteTestFinder.getAllPagesToRunForThisSuite();
    return applyPartition(allPages);
  }

  protected void appendIndices(Writer writer, WikiPage page, List<PagePosition> indices) throws IOException {
    for (PagePosition index : indices) {
      writer.append(page.getFullPath().toString())
        .append('\t');
      for (Object dimension : index.getGroup()) {
        String dimensionStr = formatDimension(dimension);
        writer.append(dimensionStr).append('\t');
      }
      writer.append(Integer.toString(index.getPositionInGroup())).append('\n');
    }
  }

  protected String formatDimension(Object dimension) {
    if (dimension instanceof WikiPageIdentity) {
      return ((WikiPageIdentity) dimension).testSystem();
    } else {
      return String.valueOf(dimension);
    }
  }

  protected PagePositions applyPartition(List<WikiPage> pages) {
    return context.testRunFactoryRegistry.findPagePositions(pages);
  }
}
