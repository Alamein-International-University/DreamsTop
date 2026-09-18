package com.dreamstop.server.handler;

import com.dreamstop.common.dto.ContributionDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

/****
 * Handles contribution requests to wishlist items.
 *
 ****
 * Note *
 * I used STUB to return fixed dummy data to isolate and test this component
 ****
 *
 */

public class ContributionRequestHandler {

    public Response handleContribution(Request request, ClientHandler client) {
        Integer contributorId = client.resolveUserId(request);
        if (contributorId == null) {
            return Response.unauthorized("You must be logged in to contribute");
        }

        ContributionDTO contribution = request.getPayloadAs(ContributionDTO.class);
        if (contribution == null) {
            return Response.badRequest("Invalid contribution payload");
        }

        contribution.setContributorId(contributorId);

        return Response.success(contribution, "Contribution registered successfully (Stub)");
    }
}