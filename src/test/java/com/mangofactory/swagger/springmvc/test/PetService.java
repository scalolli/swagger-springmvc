package com.mangofactory.swagger.springmvc.test;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiError;
import com.wordnik.swagger.core.ApiErrors;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.sample.exception.NotFoundException;

import java.util.List;

@Controller
@RequestMapping("/pet")
@Api(value="", description="Operations about pets")
public class PetService {

	@RequestMapping(value="/{petId}",method=RequestMethod.GET)
	@ApiOperation(value = "Find pet by ID", notes = "Returns a pet when ID < 10. "
			+ "ID > 10 or nonintegers will simulate API error conditions", responseClass = "com.wordnik.swagger.sample.model.Pet"
		)
	@com.mangofactory.swagger.ApiErrors(NotFoundException.class)
	public Pet getPetById (
			@ApiParam(value = "ID of pet that needs to be fetched",  allowableValues = "range[1,5]", required = true) @PathVariable("petId") String petId)
	throws NotFoundException {
		throw new NotImplementedException();
	}

	@RequestMapping(value="/findByTags",method=RequestMethod.GET)
	@ApiOperation(value = "Finds Pets by tags", notes = "Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.", responseClass = "List[Pet]", multiValueResponse = true)
	@ApiErrors(value = { @ApiError(code = 400, reason = "Invalid tag value") })
	@Deprecated
	public List<Pet> findPetsByTags(
			@ApiParam(value = "Tags to filter by", required = true, allowMultiple = true) @RequestParam("tags") String tags) {
		throw new NotImplementedException();
	}

    @RequestMapping(value="/updatePets",method=RequestMethod.PUT)
    @ApiOperation(value = "Update pets")
    public void updatePets(
            @ApiParam(value = "Pets to update", required = true, allowMultiple = true) @RequestParam("pets") List<Pet> pets) {
        throw new NotImplementedException();
    }
}
